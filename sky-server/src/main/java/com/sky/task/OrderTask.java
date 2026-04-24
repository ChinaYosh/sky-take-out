package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.user.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderTask
{
    @Autowired
    private OrderMapper orderMapper;
    @Scheduled(cron="0  0/5 *  * * *")
    public void processTimeoutOrder()
    {
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        LocalDateTime  orderTime =  LocalDateTime.now().plusMinutes(-15);
        LambdaUpdateWrapper<Orders> wp = new LambdaUpdateWrapper<>();
         //* 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        wp.eq(Orders::getStatus,Orders.PENDING_PAYMENT)
          .lt(Orders::getOrderTime,orderTime)
          .set(Orders::getStatus,Orders.CANCELLED)
                .set(Orders::getCancelReason, MessageConstant.ORDER_TIMEOUT);
        orderMapper.update(null,wp);
    }
    @Scheduled(cron="0 0 1 * * *")
    public void processCompletionOrder()
    {
        log.info("定时处理待派送订单:{}", LocalDateTime.now());
        LocalDateTime  orderTime =  LocalDateTime.now().plusMinutes(-60);
        LambdaUpdateWrapper<Orders> wp = new LambdaUpdateWrapper<>();
        wp.eq(Orders::getStatus,Orders.DELIVERY_IN_PROGRESS)
          .lt(Orders::getOrderTime,orderTime)
          .set(Orders::getStatus,Orders.COMPLETED);
        orderMapper.update(null,wp);
    }
}
