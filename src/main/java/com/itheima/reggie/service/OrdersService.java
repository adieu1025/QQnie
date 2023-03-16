package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    /**
     * 修改订单状态
     * @param orders
     */
    void updateStatus(Orders orders);

    /**
     * 在来一单
     * @param orders
     */
    void again(Orders orders);
}
