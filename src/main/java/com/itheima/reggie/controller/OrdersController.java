package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        ordersService.submit(orders);
        return R.success("下单成功！");
    }

    /**
     * 移动端页面订单的分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> page(int page, int pageSize){
        log.info("订单分页查询...");
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        ordersService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");
        List<Orders> records = pageInfo.getRecords();

        List<OrdersDto> ordersDtos = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            //查询订单明细表
            LambdaQueryWrapper<OrderDetail> qw = new LambdaQueryWrapper<>();
            qw.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(qw);
            //把查出来的订单明细赋值给orderDto
            ordersDto.setOrderDetails(orderDetails);

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtos);

        return R.success(ordersDtoPage);
    }

    /**
     * 后台管理页面订单分页查询
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page<OrdersDto>> page(int page, int pageSize, Long number, String beginTime,String endTime){
        log.info("page:{}",page);
        log.info("pageSize:{}",pageSize);
        log.info("id:{}",number);
        log.info("beginTime:{}",beginTime);
        log.info("endTime:{}",endTime);

        //分页构造器
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        //根据id查询
        qw.eq(number != null,Orders::getNumber,number);
        //在某时间段内的
        qw.between(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime,endTime);
        //排序条件
        qw.orderByDesc(Orders::getOrderTime);

        //执行查询
        ordersService.page(pageInfo,qw);

        //以上已经查询到了Orders的信息，但是页面上还需要展示用户信息，
        // 因此我们需要用到OrdersDto，来改造页面展示内容

        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        List<Orders> records= pageInfo.getRecords();

        //遍历record，修改值
        List<OrdersDto> ordersDtos = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //对象拷贝，把普通属性拷贝给ordersDto
            BeanUtils.copyProperties(item, ordersDto);
            //根据userId查询用户
            Long userId = item.getUserId();
            User user = userService.getById(userId);
            if (user != null) {
                String userName = user.getName();
                //把用户名赋值给ordersDto
                ordersDto.setUserName(userName);
            }
            return ordersDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(ordersDtos);

        return R.success(dtoPage);
    }

    /**
     * 修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders){
        log.info(orders.toString());
        ordersService.updateStatus(orders);
        return R.success("修改订单状态成功！");
    }
}
