package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品，同时保存对应口味数据
     * @param dishDto
     */
    @Override
    @Transactional//因为涉及到两张表，所以加入事务管理，保证事务的一致性
    public void saveWithFlavor(DishDto dishDto) {

        //保存菜品的基本信息到菜品表dish,
        // 在此能直接传入dishDto是因为dishDto继承了dish，拥有dish所拥有的属性
        this.save(dishDto);

        //保存好dishDto后，dishId也相应地赋上了值，
        // 现在需要把它取出来，因为在下面保存菜品口味表时需要用到
        Long dishId = dishDto.getId();//菜品id
        //把dishId的值赋给flavors(以下是通过流的方式)
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors =  flavors.stream().map((item) ->{
           item.setDishId(dishId);
           return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //先查询菜品基本信息
        Dish dish = this.getById(id);

        //根据对象拷贝，把值赋给dishDto
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //再来查询当前菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //把flavors赋值给dishDto
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 更新菜品信息，同时更新对应的口味信息
     * @param dishDto
     */
    @Override
    @Transactional//因为涉及到两张表，所以加入事务管理，保证事务的一致性
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish信息(因为dish是dishDto的父类，因此可以直接传入dishDto)
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        //把dishId的值赋给flavors(以下是通过流的方式)
        flavors =  flavors.stream().map((item) ->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        //处理完成后进行批量保存
        dishFlavorService.saveBatch(flavors);
    }
}
