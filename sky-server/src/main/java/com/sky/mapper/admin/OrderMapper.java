package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import org.apache.coyote.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
@Repository("adminOrderMapper") // 👈 就加这一句！
public interface OrderMapper  extends BaseMapper<Orders> {

    @Select("select sum(amount) from orders  ${ew.customSqlSegment} ")
    BigDecimal sumAmont(@Param("ew") LambdaQueryWrapper<Orders> queryWrapper);


    List<GoodsSalesDTO> selectSalesTop(LocalDateTime left, LocalDateTime right);
}
