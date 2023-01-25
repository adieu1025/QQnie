package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
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
     * 订单的分页查询
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

}
