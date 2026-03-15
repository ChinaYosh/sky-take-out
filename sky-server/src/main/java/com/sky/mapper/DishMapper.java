package com.sky.mapper;

import com.sky.annotation.AutoFile;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

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
    List<DishVO> page();


    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @AutoFile(OperationType.INSERT)
    void insert(Dish dish);

    void delete( @Param("array") List<Long> id);

    @AutoFile(OperationType.UPDATE)
    void update(Dish dish);
}
