package com.canon.QQnie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canon.QQnie.entity.OrderDetail;
import com.canon.QQnie.mapper.OrderDetailMapper;
import com.canon.QQnie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}