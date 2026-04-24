package com.sky.service.user.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
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
import com.sky.service.admin.impl.WebSocketServer;
import com.sky.service.user.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null)
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
        orders.setPhone(addressBook.getPhone());
        //地址 = 省市区
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());

        //获取名称
        User user = userMapper.selectById(BaseContext.getCurrentId());
        orders.setUserId(user.getId());
        orders.setUserName(user.getName());
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
    //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.selectById(id);
        if(orders == null)
        {
            log.error("订单不存在");
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if(orders.getStatus() == Orders.COMPLETED || orders.getStatus() == Orders.CANCELLED)
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
    public PageResult history(OrdersPageQueryDTO ordersPageQueryDTO)
    {
        log.info("{}",ordersPageQueryDTO);
        //查询订单
        Page<Orders> page = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Orders::getUserId,BaseContext.getCurrentId());
        queryWrapper.eq(ordersPageQueryDTO.getStatus() != null,Orders::getStatus,ordersPageQueryDTO.getStatus());
        queryWrapper.orderByDesc(Orders::getOrderTime);

        Page<Orders> ordersPage = orderMapper.selectPage(page,queryWrapper);
        log.info("查询订单分页信息，结果：{}", ordersPage);
        //对订单进行更新菜品
        List<Orders> records = ordersPage.getRecords().stream().map(order ->
        {
            //根据book_id设置orders
            LambdaQueryWrapper<AddressBook> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.eq(AddressBook::getId,order.getAddressBookId());

            AddressBook addressBook = addressBookMapper.selectOne(wrapper2);
            //省市区具体地址
            order.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(OrderDetail::getOrderId,order.getId());
            queryWrapper1.orderByAsc(OrderDetail::getOrderId);
            List<OrderDetail> orderDetails = orderDetailmapper.selectList(queryWrapper1);
            order.setOrderDetailList(orderDetails);
            return order;
        }).toList();

        log.info("查询订单分页信息成功，结果：{}", records);
        return new PageResult(ordersPage.getTotal(),records);
    }
    //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款

    @Override
    public Orders repetition(Long id) {
        // 1. 查询原订单
        Orders oldOrder = orderMapper.selectById(id);
        if (oldOrder == null) {
            log.error("订单不存在：{}", id);
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 2. 查询原订单的购物项（菜品/套餐）
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetailList = orderDetailmapper.selectList(queryWrapper);

        // 3. 构建新订单（复制原订单信息）
        Orders newOrder = new Orders();
        BeanUtils.copyProperties(oldOrder, newOrder);
        newOrder.setId(null);

        // 4. 插入新订单
        orderMapper.insert(newOrder);

        // 5. 订单详情也复制
        for (OrderDetail detail : orderDetailList)
        {
            detail.setId(null);
            detail.setOrderId(newOrder.getId());
        }
        orderDetailmapper.insert(orderDetailList);
        log.info("订单重新下单成功，新订单：{}", newOrder);
        return newOrder;
    }

    @Override
    public void cancel(Long id) {
        // 1. 获取当前登录用户ID（必须加！否则任何人都能取消别人订单）
        Long currentUserId = BaseContext.getCurrentId();

        // 2. 构造查询条件：订单ID + 当前用户ID（核心修复！）
        LambdaQueryWrapper<Orders> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Orders::getId, id)
                .eq(Orders::getUserId, currentUserId); // 必须校验归属权

        Orders orders = orderMapper.selectOne(queryWrapper);

        // 3. 订单不存在 / 不属于当前用户
        if (orders == null) {
            log.error("订单不存在或无权限操作，订单ID：{}，用户ID：{}", id, currentUserId);
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 4. 订单状态校验（只有待支付、待接单才能取消）
        if (orders.getStatus() != Orders.PENDING_PAYMENT && orders.getStatus() != Orders.TO_BE_CONFIRMED) {
            log.error("订单状态不允许取消，订单ID：{}，当前状态：{}", id, orders.getStatus());
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 5. 更新订单（取消）
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getId, id)
                .set(Orders::getStatus, Orders.CANCELLED)
                .set(Orders::getCancelReason, "用户取消")
                .set(Orders::getCancelTime, LocalDateTime.now());

        orderMapper.update(null, updateWrapper); // 这里必须传第一个参数null！
    }

    @Override
    public Orders orderDetail(Long id)
    {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getId,id);
        Orders  orders = orderMapper.selectOne(queryWrapper);
        LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(OrderDetail::getOrderId,id);
        queryWrapper1.orderByAsc(OrderDetail::getOrderId);
        orders.setOrderDetailList(orderDetailmapper.selectList(queryWrapper1));
        orders.setOrderDishes("好吃到爆");
        return orders;
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
        orderMapper.update(null,updateOrder);
    }
}