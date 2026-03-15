package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFile;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl  implements DishService
{
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    @AutoFile(value = OperationType.INSERT)
    public DishVO save(DishDTO dishDTO)
    {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        
        dishMapper.insert(dish);
        
        // 确保获取到生成的 ID
        Long id = dish.getId();
        log.info("插入菜品后生成的 ID: {}", id);
        
        if(dishDTO.getFlavors() != null && id != null)
        {
            dishFlavorMapper.insertBatch(id, dishDTO.getFlavors());
        }
        
        var categiryName = dishMapper.getCategiryName(dish.getCategoryId());
        return DishVO.builder()
                .id(id)
                .name(dishDTO.getName())
                .categoryId(dishDTO.getCategoryId())
                .price(dishDTO.getPrice())
                .image(dishDTO.getImage())
                .description(dishDTO.getDescription())
                .status(dishDTO.getStatus())
                .updateTime(dish.getUpdateTime())
                .categoryName(categiryName)
                .flavors(dishDTO.getFlavors())
                .build();
    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO)
    {
        log.info("分页查询参数：{}", dishPageQueryDTO);
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        List<DishVO> list =dishMapper.page();

        return new PageResult(list.size(),list);
    }

    @Override
    public void delete(List<Long> id) {
        for(var it : id)
        {
            if(dishMapper.getById(it).getStatus() == StatusConstant.ENABLE)
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            if(setmealMapper.getByDishId(it) != null)
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        dishMapper.delete(id);
        dishFlavorMapper.deleteArray(id);
        log.info("删除菜品：{}", id);
    }

    @Override
    public DishVO update(DishDTO dishDTO)
    {
        log.info("更新菜品：{}", dishDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        Long id = dish.getId();
        log.info("菜品 ID: {}, 口味列表：{}", id, dishDTO.getFlavors());
        
        // 先删除旧的口味数据
        dishFlavorMapper.delete(id);
        
        // 只有当口味列表不为空时才插入
        if (dishDTO.getFlavors() != null && !dishDTO.getFlavors().isEmpty()) {
            dishFlavorMapper.insertBatch(id, dishDTO.getFlavors());
        }
        var categiryName = dishMapper.getCategiryName(dish.getCategoryId());
        return DishVO.builder()
                .id(id)
                .name(dishDTO.getName())
                .categoryId(dishDTO.getCategoryId())
                .price(dishDTO.getPrice())
                .image(dishDTO.getImage())
                .description(dishDTO.getDescription())
                .status(dishDTO.getStatus())
                .updateTime(dish.getUpdateTime())
                .categoryName(categiryName)
                .flavors(dishDTO.getFlavors())
                .build();
    }

    @Override
    public DishVO getById(Long id)
    {
        DishVO  var = dishMapper.getById(id);
        var.setFlavors(dishFlavorMapper.getById(id));
        return var;
    }

}
