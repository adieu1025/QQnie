package com.canon.QQnie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canon.QQnie.dto.SetmealDto;
import com.canon.QQnie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 修改套餐，同时更新对应的菜品信息
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto);

    /**
     * 启售、停售套餐
     * @param ids
     */
    void updateSetmealStatus(List<Long> ids);
}
