package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单概览数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOverViewVO implements Serializable {
    //待接单数量
    private Long waitingOrders;

    //待派送数量
    private Long deliveredOrders;

    //已完成数量
    private Long completedOrders;

    //已取消数量
    private Long cancelledOrders;

    //全部订单
    private Long allOrders;
}
