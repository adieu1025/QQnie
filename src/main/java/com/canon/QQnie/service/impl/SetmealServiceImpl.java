package com.canon.QQnie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.canon.QQnie.common.CustomException;
import com.canon.QQnie.dto.SetmealDto;
import com.canon.QQnie.entity.Setmeal;
import com.canon.QQnie.entity.SetmealDish;
import com.canon.QQnie.mapper.SetmealMapper;
import com.canon.QQnie.service.SetmealDishService;
import com.canon.QQnie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和饮品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存饮品的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);
        //保存套餐和饮品的关联信息，操作setmeal_dish，执行insert操作。

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时需要删除套餐和饮品的关联数据
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，确定是否可以删除，可以先想一下，sql语句怎么写
        //sql语句为：select count(*) from setmeal where id in (1,2,3) and status = 1
        //在此，若查询结果>0，说明要删除的数据中包含有 处于起售状态的套餐，不能删除。
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除！");
        }

        //如果可以删除，先删除套餐表中的数据
        this.removeByIds(ids);

        //再删除关系表中的数据
        //sql：delete from setmeal_dish where setmeal_id in(1,2,3)
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(queryWrapper1);
    }

    /**
     * 修改套餐，同时更新对应的饮品信息
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新套餐信息
        this.updateById(setmealDto);

        //清除当前套餐的饮品信息(根据套餐id去删除)
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //添加当前提交过来的饮品数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //把setmealId的值赋给setmealDishes（通过流的方式）
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //处理完成后进行批量保存
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 启售、停售套餐
     * @param ids
     */
    @Override
    public void updateSetmealStatus(List<Long> ids) {
        //根据id查询套餐
        List<Setmeal> setmeals = this.listByIds(ids);

        //修改套餐的status字段
        for(Setmeal setmeal: setmeals){
            if(setmeal.getStatus() == 1){
                setmeal.setStatus(0);
            }else {
                setmeal.setStatus(1);
            }
        }
        //更新修改的套餐
        this.updateBatchById(setmeals);
    }
}
