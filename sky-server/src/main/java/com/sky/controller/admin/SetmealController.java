package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.user.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理
 * 完全对齐苍穹外卖官方源码规范
 */
@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐管理相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * 官方源码核心接口：POST /admin/setmeal
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    @CachePut(cacheNames = "setmealCache", key = "#setmealDTO.categoryId" )
    public Result save(@RequestBody SetmealDTO setmealDTO)
    {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐分页查询
     * 官方源码核心接口：GET /admin/setmeal/page
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        //如果数据位空，设置默认为 pase = 1, size = 10;
        if (setmealPageQueryDTO.getPage() == 0 || setmealPageQueryDTO.getPageSize() == 0)
        {
            setmealPageQueryDTO.setPage(1);
            setmealPageQueryDTO.setPageSize(10);
        }
        PageResult pageResult = setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 批量删除套餐
     * 官方源码核心接口：DELETE /admin/setmeal
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐：{}", ids);
        setmealService.delete(ids);
        return Result.success();
    }

    /**
     * 根据id查询套餐（含套餐菜品）
     * 官方源码核心接口：GET /admin/setmeal/{id}
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐：{}", id);
        SetmealVO setmeal = setmealService.getById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * 官方源码核心接口：PUT /admin/setmeal
     */
    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO)
    {
        log.info("修改套餐：{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐启售/停售
     * 官方源码核心接口：POST /admin/setmeal/status/{status}
     */
    @PostMapping("/status/{status}")
    @ApiOperation("套餐启售/停售")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id)
    {
        log.info("套餐启售/停售：status={}, id={}", status, id);
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 条件查询
     * 官方源码核心接口：GET /admin/setmeal/list
     */
    @GetMapping("/list")
    @ApiOperation("条件查询")
    public Result<List<SetmealVO>> list(SetmealDTO setmeal)
    {
        log.info("条件查询：{}", setmeal);
        List<SetmealVO> list = setmealService.list(setmeal);
        return Result.success(list);
    }

}