package com.sky.mapper.user;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper
{
    public void insertBatch(@Param("dishId") Long id, @Param("flavors") List<DishFlavor> flavors);

    List<DishFlavor> getById(Long id);

    void delete( Long id);

    void deleteArray(@Param("array")List<Long> id);

    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getByDishId(Long id);
}
