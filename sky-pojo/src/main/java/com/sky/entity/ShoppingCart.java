package com.sky.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("shopping_cart")
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    //名称
    private String name;

    //用户id
    @TableField(value = "user_id")
    private Long userId;

    //菜品id
    @TableField(value = "dish_id")
    private Long dishId;

    //套餐id
    @TableField(value = "setmeal_id")
    private Long setmealId;

    //口味
    @TableField(value = "dish_flavor")
    private String dishFlavor;

    //数量
    @TableField(value = "number")
    private Integer number;

    //金额

    private BigDecimal amount;

    //图片
    private String image;

    @TableField(value = "create_time")
    private LocalDateTime createTime;
}
