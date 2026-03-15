package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishFlavorMapper
{
    public void insertBatch(@Param("dishId") Long id, @Param("flavors") List<DishFlavor> flavors);

    List<DishFlavor> getById(Long id);

    void delete( Long id);

    void deleteArray(@Param("array")List<Long> id);
}
