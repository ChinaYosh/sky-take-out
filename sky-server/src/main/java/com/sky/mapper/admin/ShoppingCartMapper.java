package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart>
{

}
