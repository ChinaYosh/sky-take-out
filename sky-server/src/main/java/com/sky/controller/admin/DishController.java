package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.Result;
import com.sky.service.admin.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController
{
    @Autowired
    private DishService dishService;
    
    @PostMapping
    public Result<DishVO> save(@RequestBody DishDTO dishDTO)
    {
        log.info("新增菜品");
        log.info("接收到的 dishDTO: {}", dishDTO);
        DishVO dishVO = dishService.save(dishDTO);
        log.info("返回的 dishVO: {}", dishVO);
        log.info("返回的 image: {}", dishVO.getImage());
        return Result.success(dishVO);
    }
    @GetMapping("/page")
    public Result page(DishPageQueryDTO dishPageQueryDTO)
    {
        log.info("分页查询");

        return Result.success(dishService.page(dishPageQueryDTO));
    }
    
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids)
    {
        log.info("批量删除菜品：{}", ids);
        dishService.delete(ids);
        return Result.success();
    }
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO)
    {
        log.info("修改菜品：{}", dishDTO);
        DishVO res =  dishService.update(dishDTO);
        return Result.success(res);
    }
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id)
    {
        log.info("查询菜品：{}", id);
        return Result.success(dishService.getById(id));
    }

}
