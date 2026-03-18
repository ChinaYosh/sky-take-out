package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public  interface SetmealDishMapper extends BaseMapper<SetmealDish>
{


    void deleteBySetmealId(Long setmealId);

     void insertBatch(List<SetmealDish> setmealDishes);
}
