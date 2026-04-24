package com.sky.controller.admin;

import com.sky.config.RedisConfig;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.admin.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController
{
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    
    @PostMapping

    @CachePut(cacheNames = "userCache" ,key = "#result.data.id")
    public Result<DishVO> save(@RequestBody DishDTO dishDTO)
    {

        log.info("接收到的 dishDTO: {}", dishDTO);
        DishVO dishVO = dishService.save(dishDTO);
        return Result.success(dishVO);
    }
    @GetMapping("/page")
    public Result page(DishPageQueryDTO dishPageQueryDTO)
    {
        log.info("分页查询");

        return Result.success(dishService.page(dishPageQueryDTO));
    }
    
    @DeleteMapping
    @CacheEvict(cacheNames = "userCache" ,allEntries = true)
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
    @Cacheable(cacheNames = "userCache" ,key = "#id")
    public Result<DishVO> getById(@PathVariable Long id)
    {
        log.info("查询菜品：{}", id);
        return Result.success(dishService.getById(id));
    }
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId)
    {
        String key = "dish_" + categoryId;
        ValueOperations<String,List<DishVO>> valueOperations = redisTemplate.opsForValue();
        List<DishVO> list = valueOperations.get(key);
        if (list == null || list.size() == 0)
        {
            Dish dish = new Dish();
            dish.setCategoryId(categoryId);
            dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品
            list = dishService.listWithFlavor(dish);
            valueOperations.set(key,list);
        }
        return Result.success(list);
    }
    /**
     * 停售或起售
     */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "userCache" ,allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id)
    {
        log.info("起售或停售：{}", id);
        DishDTO dish = new DishDTO();
        dish.setStatus(status);
        dish.setId(id);
        dishService.update(dish);
        return Result.success();
    }


}
