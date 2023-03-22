package com.canon.QQnie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canon.QQnie.dto.DishDto;
import com.canon.QQnie.entity.Dish;

import java.util.List;


public interface DishService extends IService<Dish> {
    //新增饮品，同时插入饮品对应的口味数据，需要操作两张表dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询饮品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新饮品信息，同时更新对应的口味信息
    public void updateWithFlavor(DishDto dishDto);

    //起售、停售饮品
    public void updateStatus(List<Long> ids);

    //删除饮品同时删除其口味信息(套餐中有该饮品则不能删除)
    public void removeWithFlavor(List<Long> ids);
}
