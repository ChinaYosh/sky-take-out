package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @Select("select * from  sky_take_out.setmeal_dish where  dish_id = #{id}")
    SetmealVO getByDishId(Long id);

    @Select("select sd.name,sd.copies,d.image,d.description from sky_take_out.setmeal_dish sd left join  sky_take_out.dish d on sd.dish_id = d.id where sd.setmeal_id = #{id}")
    List<DishItemVO> getDishItemBySetmealId(Long id);

    /**
     * 动态条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);
}
