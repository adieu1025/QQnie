package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        //判断购物车是否为空
        if (shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空，不能下单！");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单！");
        }

        //根据以上查出来的数据给订单orders的属性赋值

        //利用MP提供的id工具类生产一个订单号
        long ordersId = IdWorker.getId();

        //计算总金额（该对象是原子操作，可以保证多线程、高并发下的正确计算）
        AtomicInteger amount = new AtomicInteger(0);

        //遍历shoppingCarts，同时计算总金额以及生产订单明细的数据
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();

            //给orderDetail设置属性
            orderDetail.setName(item.getName());
            orderDetail.setOrderId(ordersId);
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());

            //计算总金额
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());


            return  orderDetail;
        }).collect(Collectors.toList());

        orders.setId(ordersId);//设置订单id
        orders.setNumber(String.valueOf(ordersId));//设置订单号，与订单id相同
        orders.setStatus(2);//设置订单状态，此处设置为2（已付款，待配送）
        orders.setUserId(userId);//设置用户id
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());//设置付款时间
        orders.setAmount(new BigDecimal(amount.get()));//设置总金额
        orders.setConsignee(addressBook.getConsignee());//设置收货人
        orders.setPhone(addressBook.getPhone());//设置收货人手机号
        //设置地址
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail())
        );

        //向订单表插入数据（一条数据）
        this.save(orders);

        //向订单明细表插入数据（多条数据）
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

    /**
     * 修改订单状态
     * @param orders
     */
    @Override
    public void updateStatus(Orders orders) {
        //获取当前订单id
        Long id = orders.getId();
        //根据订单id去查询对应的订单信息
        Orders orders1 = this.getById(id);
        //修改状态码
        if(orders1 != null){
            orders1.setStatus(orders.getStatus());
            //保存
            this.updateById(orders1);
        }else {
            throw new CustomException("订单不存在！");
        }
    }

    /**
     * 再来一单
     * @param orders
     */
    @Override
    public void again(Orders orders) {
        //查询当前用户id
        Long userId = BaseContext.getCurrentId();

        //根据id查询数据库该订单的商品信息
        LambdaQueryWrapper<OrderDetail> qw = new LambdaQueryWrapper<>();
        qw.eq(OrderDetail::getOrderId,orders.getId());
        List<OrderDetail> orderDetails = orderDetailService.list(qw);

        //清空当前购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(queryWrapper);

        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        //把订单明细表中的商品数据放入购物车中
        for(OrderDetail orderDetail : orderDetails){
            //创建一个新的购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            //给购物车对象赋值
            shoppingCart.setName(orderDetail.getName());//名称
            shoppingCart.setImage(orderDetail.getImage());//图片
            shoppingCart.setUserId(userId);//用户Id
            if(orderDetail.getDishId() != null){//dish数据
                shoppingCart.setDishId(orderDetail.getDishId());
                shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
            }else {//套餐数据
                shoppingCart.setSetmealId(orderDetail.getSetmealId());
            }
            shoppingCart.setNumber(orderDetail.getNumber());//份数
            shoppingCart.setAmount(orderDetail.getAmount());//总价
            shoppingCart.setCreateTime(LocalDateTime.now());//时间
            //添加到购物车集合
            shoppingCarts.add(shoppingCart);
        }
        //保存到数据库
        shoppingCartService.saveBatch(shoppingCarts);
    }
}
