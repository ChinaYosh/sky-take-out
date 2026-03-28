package com.sky.service.user.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.admin.ShoppingCartMapper;
import com.sky.mapper.admin.UserMapper;
import com.sky.mapper.user.AddressBookMapper;
import com.sky.mapper.user.OrderDetailmapper;
import com.sky.mapper.user.OrderMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.WebSocketServer;
import com.sky.service.user.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService
{
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailmapper orderDetailmapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private  WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO)
    {
        AddressBook byId = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if(byId == null)
        {
            log.error("用户地址簿数据不存在");
           throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(queryWrapper);
        if(shoppingCarts == null || shoppingCarts.isEmpty())
        {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);

        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));

        orders.setAddressBookId(ordersSubmitDTO.getAddressBookId());
        orders.setPhone(byId.getPhone());
        orders.setConsignee(byId.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());
        orderMapper.insert(orders);
        //插入shoppingcarts
      List<OrderDetail> array = shoppingCarts.stream().map(s ->
        {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orders.getId());
            BeanUtils.copyProperties(s, orderDetail);
            orderDetail.setId(null);
            return orderDetail;
        }).toList();
        orderDetailmapper.insert(array);

        LambdaQueryWrapper<ShoppingCart> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartMapper.delete(deleteWrapper);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 1.获取当前用户
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);


        // 5.构造模拟支付返回值
        OrderPaymentVO vo = OrderPaymentVO.builder()
                .nonceStr("test")
                .signType("MD5")
                .paySign("test")
                .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                .packageStr("prepay_id=test")
                .build();
        log.info("模拟支付成功：{}", vo);
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     *
     */
    @Override
    public void paySuccess(String outTradeNo)
    {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, ordersDB.getId())
                .set(Orders::getStatus, Orders.TO_BE_CONFIRMED)
                .set(Orders::getCheckoutTime,  LocalDateTime.now())
                .set(Orders::getPayStatus,  Orders.PAID);

        orderMapper.update(updateWrapper);
        //传递前段
        Map map = new HashMap();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号:" + outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
        log.info("订单支付成功，订单号：{}", outTradeNo);
    }

    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.selectById(id);
        if(orders == null)
        {
            log.error("订单不存在");
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orders.getStatus() != Orders.DELIVERY_IN_PROGRESS)
        {
            log.error("订单状态不正确");
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号:" + orders.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
        log.info("订单提醒成功，订单号：{}", id);
    }

    @Override
    public PageResult history()
    {
     return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        // 根据订单ID查询订单
        Orders ordersDB = orderMapper.selectById(ordersCancelDTO.getId());
        if (ordersDB == null) {
            log.error("订单不存在");
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 检查订单状态是否允许取消
        if (ordersDB.getStatus() != Orders.PENDING_PAYMENT && ordersDB.getStatus() != Orders.TO_BE_CONFIRMED) {
            log.error("订单状态不允许取消");
            throw new OrderBusinessException(MessageConstant.UNKNOWN_ERROR);
        }

        // 更新订单状态为已取消，并设置取消原因
        LambdaUpdateWrapper<Orders> updateOrder = new LambdaUpdateWrapper<>();
        updateOrder.eq(Orders::getId, ordersDB.getId())
                .set(Orders::getStatus, Orders.CANCELLED)
                .set(Orders::getCancelReason, ordersCancelDTO.getCancelReason());
        orderMapper.update(updateOrder);
    }
}