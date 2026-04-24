package com.sky.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.annotation.AutoFile;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.user.DishFlavorMapper;
import com.sky.mapper.admin.DishMapper;
import com.sky.mapper.admin.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("adminDishService")
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

        if(dishDTO.getFlavors() != null && !dishDTO.getFlavors().isEmpty() && id != null)
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

        //mbatis-plus分页查询
        Page<DishVO> pageParam = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> list =dishMapper.page(pageParam,dishPageQueryDTO);
        log.info("分页查询结果：{}", list);
        List<DishVO> records = list.getRecords();
        return new PageResult(list.getTotal(), records);
    }

    @Override
    public void delete(List<Long> id) {
        for(var it : id)
        {
            if(dishMapper.getById(it).getStatus().equals(StatusConstant.ENABLE))
            {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            if(setmealMapper.getByDishId(it) != null)
            {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
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

    @Override
    public List<DishVO> listWithFlavor(Dish dish)
    {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
