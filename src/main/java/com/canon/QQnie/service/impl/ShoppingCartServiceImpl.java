package com.canon.QQnie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canon.QQnie.entity.ShoppingCart;
import com.canon.QQnie.mapper.ShoppingCartMapper;
import com.canon.QQnie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
