package com.canon.QQnie.dto;

import com.canon.QQnie.entity.Setmeal;
import com.canon.QQnie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
