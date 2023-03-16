package com.canon.QQnie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canon.QQnie.entity.User;
import com.canon.QQnie.mapper.UserMapper;
import com.canon.QQnie.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
