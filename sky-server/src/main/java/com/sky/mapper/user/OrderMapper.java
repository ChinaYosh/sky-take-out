package com.sky.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Orders>
{
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);
}
