package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.annotation.AutoFile;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper  extends BaseMapper<Setmeal> {
    

    void delete(@Param("ids") List<Long> ids);

    Page<SetmealVO> page(@Param("page") Page<SetmealVO> page, @Param("dto") SetmealPageQueryDTO dto);

    @AutoFile(OperationType.UPDATE)
    void update(Setmeal setmeal);

    @AutoFile(OperationType.INSERT)
    @Override
    int insert(Setmeal setmeal);

    List<Setmeal> listByCategoryId(Long categoryId);

    List<Setmeal> listByStatus(Integer status);

    void insertWithFields(@Param("setmeal") SetmealDTO setmeal);

    Integer existsById(Long id);

    Integer getTotalCount();
    @Select("SELECT COUNT(id) FROM setmeal WHERE category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @Select("SELECT * FROM sky_take_out.setmeal_dish WHERE dish_id = #{id}")
    SetmealVO getByDishId(Long id);

    SetmealVO getById(Long id);


    List<SetmealDish> getDishItemBySetmealId(Long id);
    void insertBatch(@Param("setId") Long setId, @Param("dishes") List<?> dishes);

    @Select("SELECT SUM(order_count) AS totalCount, SUM(order_amount) AS totalAmount FROM setmeal")
    Map<String, Object> statistics();

    List<SetmealVO> list(SetmealDTO setmeal);
}
