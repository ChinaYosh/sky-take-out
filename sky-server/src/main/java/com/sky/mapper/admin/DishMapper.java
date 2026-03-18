package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.annotation.AutoFile;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @Select("select name from category where id = #{categoryId}")
    String getCategiryName(Long categoryId);

    @Select("select * from dish where id = #{id}")
    DishVO getById(Long id);

    Page<DishVO> page(@Param("page") Page<DishVO> page, @Param("dto") DishPageQueryDTO dto);


    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @AutoFile(OperationType.INSERT)
    @Override
    int insert(Dish dish);

    void delete( @Param("array") List<Long> id);

    @AutoFile(OperationType.UPDATE)
    void update(Dish dish);


    List<Dish> list(Dish dish);


    void increaseSales(List<SetmealDish> setmealDishes);



    void decreaseSales(@Param("dishId") List<Long> ids);
}
