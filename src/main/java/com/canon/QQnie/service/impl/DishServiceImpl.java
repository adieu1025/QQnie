package com.canon.QQnie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canon.QQnie.common.CustomException;
import com.canon.QQnie.dto.DishDto;
import com.canon.QQnie.entity.Dish;
import com.canon.QQnie.entity.DishFlavor;
import com.canon.QQnie.entity.SetmealDish;
import com.canon.QQnie.mapper.DishMapper;
import com.canon.QQnie.service.DishFlavorService;
import com.canon.QQnie.service.DishService;
import com.canon.QQnie.service.SetmealDishService;
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

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增饮品，同时保存对应口味数据
     * @param dishDto
     */
    @Override
    @Transactional//因为涉及到两张表，所以加入事务管理，保证事务的一致性
    public void saveWithFlavor(DishDto dishDto) {

        //保存饮品的基本信息到饮品表dish,
        // 在此能直接传入dishDto是因为dishDto继承了dish，拥有dish所拥有的属性
        this.save(dishDto);

        //保存好dishDto后，dishId也相应地赋上了值，
        // 现在需要把它取出来，因为在下面保存饮品口味表时需要用到
        Long dishId = dishDto.getId();//饮品id
        //把dishId的值赋给flavors(以下是通过流的方式)
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors =  flavors.stream().map((item) ->{
           item.setDishId(dishId);
           return item;
        }).collect(Collectors.toList());

        //保存饮品口味数据到饮品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询饮品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //先查询饮品基本信息
        Dish dish = this.getById(id);

        //根据对象拷贝，把值赋给dishDto
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //再来查询当前饮品对应的口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //把flavors赋值给dishDto
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 更新饮品信息，同时更新对应的口味信息
     * @param dishDto
     */
    @Override
    @Transactional//因为涉及到两张表，所以加入事务管理，保证事务的一致性
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish信息(因为dish是dishDto的父类，因此可以直接传入dishDto)
        this.updateById(dishDto);

        //清理当前饮品对应口味数据---dish_flavor表的delete操作
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


    /**
     * 启售、停售饮品
     * @param ids
     */
    @Override
    public void updateStatus(List<Long> ids) {
        //根据ids获取到饮品数据
        List<Dish> dishes = this.listByIds(ids);

        //进行饮品的状态的修改
        for(Dish dish : dishes){
            if(dish.getStatus() == 0){
                dish.setStatus(1);
            }else {
                dish.setStatus(0);
            }
        }
        //更新修改的饮品
        this.updateBatchById(dishes);
    }

    /**
     * //删除饮品同时删除其口味信息(套餐中有该饮品则不能删除)
     * @param ids
     */
    @Override
    public void removeWithFlavor(List<Long> ids) {
        //查询setmeal_dish表，饮品是否被包含在某套餐中
        LambdaQueryWrapper<SetmealDish> qw = new LambdaQueryWrapper<>();
        qw.in(SetmealDish::getDishId,ids);
        int count  = setmealDishService.count(qw);
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("饮品已经被包含在套餐中，不能删除！");
        }

        //若可以删除，先删除饮品的数据
        this.removeByIds(ids);

        //再删除饮品口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper);
    }
}
