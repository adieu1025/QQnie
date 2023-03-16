package com.canon.QQnie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canon.QQnie.entity.Employee;
import com.canon.QQnie.mapper.EmployeeMapper;
import com.canon.QQnie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
