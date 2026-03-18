package com.sky.service.user;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;
import java.util.Map;

public interface SetmealService {

    /**
     * 条件查询
     *
     * @param setmeal 查询条件
     * @return 套餐列表
     */
    List<SetmealVO> list(SetmealDTO setmeal);

    /**
     * 根据ID查询菜品项
     *
     * @param id 套餐ID
     * @return 菜品项列表
     */
    List<SetmealDish> getDishItemById(Long id);

    /**
     * 新增套餐
     * @param setmeal 套餐数据
     */
    void save(SetmealDTO setmeal);

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO 分页条件
     * @return 分页结果
     */
    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 删除套餐
     * @param ids 套餐ID列表
     */
    void delete(List<Long> ids);

    /**
     * 根据ID查询套餐详情
     * @param id 套餐ID
     * @return 套餐详情
     */
    SetmealVO getById(Long id);

    /**
     * 修改套餐信息
     * @param setmealDTO 套餐数据
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 启用/停用套餐
     * @param status 状态
     * @param id 套餐ID
     */
    void startOrStop(Integer status, Long id);

    /**
     * 按分类ID查询套餐列表
     * @param categoryId 分类ID
     * @return 套餐列表
     */
    List<Setmeal> listByCategoryId(Long categoryId);

    /**
     * 按状态查询套餐列表
     * @param status 状态码
     * @return 套餐列表
     */
    List<Setmeal> listByStatus(Integer status);

    /**
     * 检查套餐是否存在
     * @param id 套餐ID
     * @return 是否存在
     */
    boolean existsById(Long id);

    /**
     * 获取套餐总数
     * @return 套餐总数
     */
    Integer getTotalCount();

    Map<String, Object> statistics();
}
