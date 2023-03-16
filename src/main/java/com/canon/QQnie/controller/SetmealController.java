package com.canon.QQnie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.canon.QQnie.common.R;
import com.canon.QQnie.dto.DishDto;
import com.canon.QQnie.dto.SetmealDto;
import com.canon.QQnie.entity.Category;
import com.canon.QQnie.entity.Dish;
import com.canon.QQnie.entity.Setmeal;
import com.canon.QQnie.entity.SetmealDish;
import com.canon.QQnie.service.CategoryService;
import com.canon.QQnie.service.DishService;
import com.canon.QQnie.service.SetmealDishService;
import com.canon.QQnie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    /**
     * 保存套餐
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("保存套餐成功！");
    }

    /**
     * 套餐信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //条件过滤器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //过滤条件
        queryWrapper.like(name != null,Setmeal::getName,name);
        //排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");

        List<Setmeal> records = pageInfo.getRecords();

        //遍历records，修改为能在页面正确显示的值
        List<SetmealDto> list = records.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            Category category = categoryService.getById(item.getCategoryId());

            if (category != null){
                setmealDto.setCategoryName(category.getName());
            }

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    /**
     * 根据id删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功！");
    }


    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return R.success(setmealList);
    }

    /**
     * 根据id查询套餐及其包含的菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        log.info("套餐id：{}",id);
        //根据id查询套餐信息
        Setmeal setmeal = setmealService.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        //把分类名称赋值给setmealDto
        Category category = categoryService.getById(setmeal.getCategoryId());
        if (category != null){
            setmealDto.setCategoryName(category.getName());
        }
        //查询菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        //把查出来的菜品信息赋值给setmealDto
        if (setmealDishes != null){
            setmealDto.setSetmealDishes(setmealDishes);
        }

        return R.success(setmealDto);
    }


    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改套餐成功！");
    }

    /**
     * 根据套餐id获取套餐的全部菜品
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> getDishesById(@PathVariable Long id){
        //根据套餐id查套餐中的菜品信息----setmeal_dish表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);

        //获取套餐中所含菜品的id
        List<Long> dishIds = new ArrayList<>();
        for(SetmealDish setmealDish : setmealDishes){
            dishIds.add(setmealDish.getDishId());
        }

        //根据id集合查询dishes
        List<Dish> dishes = dishService.listByIds(dishIds);
        List<DishDto> dishDtoList = dishes.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            return dishDto;
        }).collect(Collectors.toList());

        //把份数信息赋值给dishDto
        for(int i = 0;i < dishDtoList.size();i++){
            Integer copies = setmealDishes.get(i).getCopies();
            dishDtoList.get(i).setCopies(copies);
        }

        return R.success(dishDtoList);
    }

    /**
     * 停售套餐
     * @param ids
     * @return
     */
    @RequestMapping("/status/0")
    public R<String> stopSelling(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.updateSetmealStatus(ids);

        return R.success("停售套餐成功！");
    }

    /**
     * 启售套餐
     * @param ids
     * @return
     */
    @RequestMapping("/status/1")
    public R<String> startSelling(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.updateSetmealStatus(ids);

        return R.success("启售套餐成功！");
    }




}
