package com.canon.QQnie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.canon.QQnie.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
