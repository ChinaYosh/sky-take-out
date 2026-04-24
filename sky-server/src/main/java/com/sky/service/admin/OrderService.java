package com.sky.service.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;

public interface OrderService
{
    void updateByOrderId(OrdersCancelDTO ordersCancelDTO);

    OrderStatisticsVO statistics();

    void complete(Long id);

    void reject(OrdersRejectionDTO id);


    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    Object selectOrderByid(Long id);

    void delivery(Long id);

    PageResult selectQueryPage(OrdersPageQueryDTO page);
}
