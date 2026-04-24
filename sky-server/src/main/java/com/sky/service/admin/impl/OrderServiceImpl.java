package com.sky.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.admin.OrderMapper;
import com.sky.mapper.user.AddressBookMapper;
import com.sky.mapper.user.OrderDetailmapper;
import com.sky.result.PageResult;
import com.sky.service.admin.OrderService;
import com.sky.vo.OrderStatisticsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service("adminOrderService")
@Slf4j
public class OrderServiceImpl  extends ServiceImpl<OrderMapper,Orders> implements OrderService
{


    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     */

    @Autowired
    private OrderDetailmapper orderDetailmapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Override
    public void updateByOrderId(OrdersCancelDTO ordersCancelDTO)
    {
        LambdaUpdateWrapper<Orders>  wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Orders::getCancelReason,ordersCancelDTO.getCancelReason())
                .set(Orders::getPayStatus,Orders.CANCELLED)
                .eq(Orders::getId,ordersCancelDTO.getId());
        baseMapper.update(null,wrapper);
        log.info("取消订单成功");

    }

    @Override
    public OrderStatisticsVO statistics()
    {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        // 2 3 4
        wrapper.groupBy(Orders::getPayStatus);


        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO(
                baseMapper.selectCount(wrapper.eq(Orders::getPayStatus, Orders.TO_BE_CONFIRMED)),
                baseMapper.selectCount(wrapper.eq(Orders::getPayStatus, Orders.CONFIRMED)),
                baseMapper.selectCount(wrapper.eq(Orders::getPayStatus, Orders.DELIVERY_IN_PROGRESS))
        );

        return orderStatisticsVO;
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id)
    {
        LambdaUpdateWrapper<Orders> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Orders::getStatus,Orders.COMPLETED)
                .eq(Orders::getId,id);
        baseMapper.update(null,wrapper);
        log.info("完成订单成功");
    }

    /**
     * 拒单
     * @param
     */
    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        LambdaUpdateWrapper<Orders> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Orders::getStatus,Orders.CANCELLED)
                .set(Orders::getRejectionReason,ordersRejectionDTO.getRejectionReason())
                .eq(Orders::getId,ordersRejectionDTO.getId());
        baseMapper.update(null,wrapper);
        log.info("拒单成功");
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        LambdaUpdateWrapper<Orders> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Orders::getStatus,Orders.CONFIRMED)
                .eq(Orders::getId,ordersConfirmDTO.getId());
        baseMapper.update(null,wrapper);
        log.info("确认订单成功");
    }

    @Override
    public Orders selectOrderByid(Long id)
    {
        LambdaQueryWrapper<Orders> wrapper1 = new LambdaQueryWrapper<>();
        Orders orders = baseMapper.selectOne(wrapper1.eq(Orders::getId, id));
        log.info("订单信息：{}",orders);
        if (orders == null)
        {
            throw new RuntimeException("订单不存在");
        }
        //根据book_id设置orders
        LambdaQueryWrapper<AddressBook> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(AddressBook::getId,orders.getAddressBookId());

        AddressBook addressBook = addressBookMapper.selectOne(wrapper2);
        //省市区具体地址
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());


        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDetail::getOrderId,id);
        List<OrderDetail> orderDetail = orderDetailmapper.selectList(wrapper);//获取菜品信息
        orders.setOrderDetailList(orderDetail);
        orders.setOrderDishes("好吃到爆");
        return orders;
    }

    @Override
    public void delivery(Long id)
    {
        LambdaUpdateWrapper<Orders> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Orders::getStatus,Orders.DELIVERY_IN_PROGRESS)
                .eq(Orders::getId,id);
        baseMapper.update(null,wrapper);
    }

    @Override
    public PageResult selectQueryPage(OrdersPageQueryDTO pageDTO)
    {
        Page<Orders> pageInfo = new Page<>(pageDTO.getPage(),pageDTO.getPageSize());
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .eq(pageDTO.getStatus() != null, Orders::getStatus, pageDTO.getStatus())
                .like(StringUtils.hasText(pageDTO.getNumber()), Orders::getNumber, pageDTO.getNumber())
                .like(StringUtils.hasText(pageDTO.getPhone()), Orders::getPhone, pageDTO.getPhone())
                .eq(pageDTO.getUserId() != null, Orders::getUserId, pageDTO.getUserId())
                .ge(pageDTO.getBeginTime() != null, Orders::getOrderTime, pageDTO.getBeginTime())
                .le(pageDTO.getEndTime() != null, Orders::getOrderTime, pageDTO.getEndTime())
                .orderByDesc(Orders::getOrderTime); // 按时间倒序
        Page<Orders> pageResult = baseMapper.selectPage(pageInfo,wrapper);
        PageResult result = new PageResult(pageResult.getTotal(), pageResult.getRecords());
        return result;
    }
}
