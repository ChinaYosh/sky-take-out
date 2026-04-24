package com.sky.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.Orders;

import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.mapper.admin.DishMapper;
import com.sky.mapper.admin.OrderMapper;
import com.sky.mapper.admin.SetmealMapper;
import com.sky.mapper.admin.UserMapper;
import com.sky.service.admin.WorkspaceService;
import com.sky.service.user.OrderService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private OrderService orderService;

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDate begin, LocalDate end) {
        /**
         * 营业额：当日已完成订单的总金额
         * 有效订单：当日已完成订单的数量
         * 订单完成率：有效订单数 / 总订单数
         * 平均客单价：营业额 / 有效订单数
         * 新增用户：当日新增用户的数量
         */

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //查询总订单数
       LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
       queryWrapper.between(Orders::getOrderTime,beginTime,endTime);
       Long totalOrderCount = orderMapper.selectCount(queryWrapper);
        //营业额
        BigDecimal turnover = orderMapper.sumAmont(new LambdaQueryWrapper<Orders>()
        .eq(Orders::getStatus, Orders.COMPLETED)
        .between(Orders::getOrderTime,beginTime,endTime)
        );
        turnover = turnover == null? BigDecimal.ZERO : turnover;

        //有效订单数
        Long validOrderCount = orderMapper.selectCount(new LambdaQueryWrapper<Orders>()
        .eq(Orders::getStatus, Orders.COMPLETED).between(Orders::getOrderTime,beginTime,endTime)
        );

        BigDecimal unitPrice = BigDecimal.ZERO;

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0 && validOrderCount != 0){
            //订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            //平均客单价
            unitPrice = turnover.divide(new BigDecimal(validOrderCount),2,BigDecimal.ROUND_HALF_UP);
        }

        //新增用户数
        Long newUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
        .between(User::getCreateTime,beginTime,endTime));

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView() {

          //  * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7 拒单
        //待接单 mybatis-plus
        Long waitingOrders = orderMapper.selectCount(
                        new LambdaQueryWrapper<Orders>().eq(Orders::getStatus,Orders.TO_BE_CONFIRMED));


        //待派送
         Long toBeConfirmed = orderMapper.selectCount(
                 new LambdaQueryWrapper<Orders>().eq(Orders::getStatus,Orders.TO_BE_CONFIRMED)
         );

        //已完成
        Long deliveredOrders = orderMapper.selectCount(
                new LambdaQueryWrapper<Orders>().eq(Orders::getStatus,Orders.DELIVERY_IN_PROGRESS)
        );

        //已取消
         Long cancelledOrders = orderMapper.selectCount(
                 new LambdaQueryWrapper<Orders>().eq(Orders::getStatus,Orders.CANCELLED)
         );

        //全部订单
        Long allOrders = orderMapper.selectCount(null);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(toBeConfirmed)
                .completedOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    public DishOverViewVO getDishOverView() {

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, StatusConstant.ENABLE);
        Long sold = dishMapper.selectCount(queryWrapper);


         Long discontinued = dishMapper.selectCount(new LambdaQueryWrapper<Dish>().eq(Dish::getStatus, StatusConstant.DISABLE));

        log.info("套餐总览：{}",sold);
        log.info("套餐总览：{}",discontinued);
        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    public SetmealOverViewVO getSetmealOverView() {
         LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
         queryWrapper.eq(Setmeal::getStatus, StatusConstant.ENABLE);
         Long sold = setmealMapper.selectCount(queryWrapper);

         Long discontinued = setmealMapper.selectCount(
                 new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getStatus, StatusConstant.DISABLE));
         log.info("套餐总览：{}",sold);
         log.info("套餐总览：{}",discontinued);
        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
