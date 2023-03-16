package com.canon.QQnie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canon.QQnie.dto.DishDto;
import com.canon.QQnie.entity.Dish;

import java.util.List;


public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息，同时更新对应的口味信息
    public void updateWithFlavor(DishDto dishDto);

    //起售、停售菜品
    public void updateStatus(List<Long> ids);

    //删除菜品同时删除其口味信息(套餐中有该菜品则不能删除)
    public void removeWithFlavor(List<Long> ids);
}
