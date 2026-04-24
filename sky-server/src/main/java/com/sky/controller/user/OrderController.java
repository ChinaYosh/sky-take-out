package com.sky.controller.user;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.Result;
import com.sky.service.user.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.ApiOperation;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
public class OrderController
{
    @Autowired
    private OrderService orderService;
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO)
    {
        log.info("用户下单：{}", ordersSubmitDTO);
        try
        {
            OrderSubmitVO res = orderService.submitOrder(ordersSubmitDTO);
            return  Result.success(res);
        } catch (Exception e)
        {
            return Result.error(e.getMessage());
        }

    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    /**
     * 催单
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id)
    {
        orderService.reminder(id);
        log.info("用户催单：{}", id);
        return Result.success();
    }
    //历史记录
    @PostMapping("/historyOrders")
    public Result history(@RequestBody OrdersPageQueryDTO ordersPageQueryDTO)
    {
        log.info("查询历史订单：{}", ordersPageQueryDTO);
        return Result.success(orderService.history(ordersPageQueryDTO));
    }
    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    public Result<Orders> repetition(@PathVariable Long id) {
        log.info("再来一单：{}", id);
        Orders orders = orderService.repetition(id); // 这里要返回新订单ID
        return Result.success(orders);
    }
    /**
     * 取消订单
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id)
    {
        log.info("取消订单：{}", id);
        orderService.cancel(id);
        return Result.success();
    }

    @GetMapping("/orderDetail/{id}")
    public Result orderDetail(@PathVariable("id") Long id)
    {
        log.info("查询订单详情：{}", id);
        return Result.success(orderService.orderDetail(id));
    }

}
