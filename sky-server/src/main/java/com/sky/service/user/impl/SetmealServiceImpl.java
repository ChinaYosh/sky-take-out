package com.sky.service.user.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish; // ✅ 新增导包
import com.sky.mapper.admin.DishMapper;
import com.sky.mapper.admin.SetmealMapper;
import com.sky.mapper.admin.SetmealDishMapper; // ✅ 新增导包 (如果你的包名不同请根据IDEA提示Alt+Enter导入)
import com.sky.mapper.user.DishFlavorMapper;
import com.sky.result.PageResult;
import com.sky.service.user.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    // ✅ 新增：注入 setmealDishMapper，否则 update 方法里调不到它会报红
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 条件查询套餐列表
     *
     * @param setmeal 查询条件
     * @return 套餐列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<SetmealVO> list(SetmealDTO setmeal) {
        try {
            List<SetmealVO> list = setmealMapper.list(setmeal);
            log.debug("查询到套餐列表：{}", list);
            return list;
        } catch (Exception e) {
            log.error("查询套餐列表失败：{}", e.getMessage(), e);
            throw new RuntimeException("查询套餐失败", e);
        }
    }

    /**
     * 根据ID查询菜品项
     *
     * @param id 套餐ID
     * @return 菜品项列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<SetmealDish> getDishItemById(Long id) {
        if (id == null || id <= 0) {
            log.warn("无效的套餐ID：{}", id);
            throw new IllegalArgumentException("套餐ID不能为空");
        }
        try {
            List<SetmealDish> items = setmealMapper.getDishItemBySetmealId(id);
            log.debug("查询到套餐菜品项：{}", items);
            return items;
        } catch (Exception e) {
            log.error("查询菜品项失败：{}", e.getMessage(), e);
            throw new RuntimeException("查询菜品项失败", e);
        }
    }

    /**
     * 新增套餐
     * @param setmeal 套餐数据
     */
    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        if (setmealDTO == null || setmealDTO.getSetmealDishes() == null || setmealDTO.getSetmealDishes().isEmpty()) {
            log.warn("提供的套餐数据无效");
            throw new IllegalArgumentException("套餐数据不能为空");
        }
        try {
            Setmeal setmeal = new Setmeal();

            // 2. 将 DTO 的属性拷贝到 Entity 中 (极其重要的一步！)
            BeanUtils.copyProperties(setmealDTO, setmeal);
            // 3. 将 Entity 传给 Mapper。这样 AOP 就能在 Entity 里找到 setCreateTime 等方法了！
            setmealMapper.insert(setmeal);
            setmealMapper.insertBatch(setmeal.getId(), setmealDTO.getSetmealDishes());
        } catch (Exception e) {
            log.error("新增套餐时发生错误：{}", e.getMessage(), e);
            throw new RuntimeException("新增套餐失败", e);
        }
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO 分页条件
     * @return 分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        try {
           Page<SetmealVO> page = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

           Page list = setmealMapper.page(page,setmealPageQueryDTO);
            PageResult pageResult = new PageResult(list.getTotal(), list.getRecords());
            log.debug("分页查询结果：{}", pageResult);
            return pageResult;
        } catch (Exception e) {
            log.error("分页查询套餐失败：{}", e.getMessage(), e);
            throw new RuntimeException("分页查询失败", e);
        }
    }

    /**
     * 删除套餐
     * @param ids 套餐ID列表
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("删除的ID列表为空");
            throw new IllegalArgumentException("删除的套餐ID不能为空");
        }
        try {
            setmealMapper.delete(ids);
            log.info("删除的套餐ID列表：{}", ids);
        } catch (Exception e) {
            log.error("删除套餐失败：{}", e.getMessage(), e);
            throw new RuntimeException("删除套餐失败", e);
        }
    }

    /**
     * 根据ID查询套餐详情
     * @param id 套餐ID
     * @return 套餐详情
     */
    @Override
    @Transactional(readOnly = true)
    public SetmealVO getById(Long id) {
        if (id == null || id <= 0) {
            log.warn("无效的套餐ID：{}", id);
            throw new IllegalArgumentException("套餐ID不能为空");
        }
        try {
            SetmealVO setmealVO = setmealMapper.getById(id);
         //获取相关菜品
            setmealVO.setSetmealDishes(setmealMapper.getDishItemBySetmealId(id));

            log.debug("查询到套餐详情：{}", setmealVO);

            return setmealVO;
        } catch (Exception e) {
            log.error("查询套餐详情失败：{}", e.getMessage(), e);
            throw new RuntimeException("查询套餐详情失败", e);
        }
    }

    /**
     * 修改套餐信息
     * @param setmealDTO 套餐数据
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        // 1. 修改套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        // 2. 获取当前套餐的 ID
        Long setmealId = setmealDTO.getId();

        // 3. 删除原本关联的套餐菜品关系 (delete from setmeal_dish where setmeal_id = ?)
        setmealDishMapper.deleteBySetmealId(setmealId);

        // 4. 获取前端传过来的最新套餐关联菜品列表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        // 5. 遍历给每一个菜品关联上套餐 ID，并批量插入
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            // 批量插入
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 启用/停用套餐
     * @param status 状态
     * @param id 套餐ID
     */
    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        if (id == null || id <= 0 || status == null) {
            log.warn("无效的启停参数");
            throw new IllegalArgumentException("参数不能为空");
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();

        try {
            setmealMapper.update(setmeal);
            log.info("更新套餐状态：ID={}, 状态={}", id, status);
        } catch (Exception e) {
            log.error("更新套餐状态失败：{}", e.getMessage(), e);
            throw new RuntimeException("更新套餐状态失败", e);
        }
    }

    /**
     * 按分类ID查询套餐列表
     * @param categoryId 分类ID
     * @return 套餐列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Setmeal> listByCategoryId(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            log.warn("无效的分类ID：{}", categoryId);
            throw new IllegalArgumentException("分类ID不能为空");
        }
        try {
            List<Setmeal> list = setmealMapper.listByCategoryId(categoryId);
            log.debug("按分类查询套餐列表：{}", list);
            return list;
        } catch (Exception e) {
            log.error("按分类查询套餐失败：{}", e.getMessage(), e);
            throw new RuntimeException("按分类查询套餐失败", e);
        }
    }

    /**
     * 按状态查询套餐列表
     * @param status 状态码
     * @return 套餐列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<Setmeal> listByStatus(Integer status) {
        if (status == null) {
            log.warn("无效的状态码");
            throw new IllegalArgumentException("状态码不能为空");
        }
        try {
            List<Setmeal> list = setmealMapper.listByStatus(status);
            log.debug("按状态查询套餐列表：{}", list);
            return list;
        } catch (Exception e) {
            log.error("按状态查询套餐失败：{}", e.getMessage(), e);
            throw new RuntimeException("按状态查询套餐失败", e);
        }
    }

    /**
     * 检查套餐是否存在
     * @param id 套餐ID
     * @return 是否存在
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        if (id == null || id <= 0) {
            log.warn("无效的套餐ID");
            throw new IllegalArgumentException("套餐ID不能为空");
        }
        try {
            Integer count = setmealMapper.existsById(id);
            boolean exists = count != null && count > 0;
            log.debug("检查套餐是否存在：ID={}, 存在={}", id, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查套餐是否存在失败：{}", e.getMessage(), e);
            throw new RuntimeException("检查套餐是否存在失败", e);
        }
    }

    /**
     * 获取套餐总数
     * @return 套餐总数
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getTotalCount() {
        try {
            Integer count = setmealMapper.getTotalCount();
            log.debug("获取套餐总数：{}", count);
            return count;
        } catch (Exception e) {
            log.error("获取套餐总数失败：{}", e.getMessage(), e);
            throw new RuntimeException("获取套餐总数失败", e);
        }
    }

    @Override
    public Map<String, Object> statistics() {
        return setmealMapper.statistics();
    }
}