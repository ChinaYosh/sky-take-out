package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.Result;
import com.sky.service.admin.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController
{
    @Autowired
    private OrderServiceImpl orderService;

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO)
    {
        log.info("取消订单：{}", ordersCancelDTO);
        orderService.updateByOrderId(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 订单统计
     * @return
     */
    @GetMapping("/statistics")
    public Result statistics()
    {
        return Result.success(orderService.statistics());
    }
    /**
     * 完成订单
     */
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id)
    {
        orderService.complete(id);
        return Result.success();
    }
    /**
     * 拒单
     */
    @PutMapping("/rejection")
    public Result reject(@RequestBody OrdersRejectionDTO ordersRejectionDTO)
    {
        orderService.reject(ordersRejectionDTO);
        return Result.success();
    }
    /**
     * 接单
     */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO)
    {
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }
    /**
     * 查询订单
     */
    @GetMapping("/details/{id}")
    public Result details(@PathVariable Long id)
    {
        log.info("查询订单：{}", id);
        return Result.success(orderService.selectOrderByid(id));
    }
    /**
     * 派送订单
     */
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id)
    {
        orderService.delivery(id);
        return Result.success();
    }
    /**
     * 订单列表
     */
    @GetMapping("/conditionSearch")
    public Result list(OrdersPageQueryDTO page)
    {
        return Result.success(orderService.selectQueryPage(page));
    }
    /**
     * 拒单
     */
}
