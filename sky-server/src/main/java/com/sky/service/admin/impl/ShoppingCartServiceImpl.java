package com.sky.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.admin.DishMapper;
import com.sky.mapper.admin.SetmealMapper;
import com.sky.mapper.admin.ShoppingCartMapper;
import com.sky.service.admin.ShoppingCartService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService
{
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO)
    {
        log.info("添加购物车,{}",shoppingCartDTO);
        LambdaQueryWrapper<ShoppingCart> wapper = new LambdaQueryWrapper<>();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //对于DTO字段，有就进行拼接，没有就忽视
        wapper.eq(ShoppingCart::getUserId, shoppingCart.getUserId());
        wapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        wapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        wapper.eq(ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        ShoppingCart cart = shoppingCartMapper.selectOne(wapper);

        if(cart != null)
        {
            cart.setNumber(cart.getNumber() + 1);
            cart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.update(cart,wapper);
        }
        else
        {
            cart = new ShoppingCart();
            BeanUtils.copyProperties(shoppingCartDTO,cart);
            cart.setUserId(BaseContext.getCurrentId());
            if(cart.getDishId() != null)
            {
                LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Dish::getId, cart.getDishId());
                Dish dish = dishMapper.selectOne(queryWrapper);
                cart.setName(dish.getName());
                cart.setImage(dish.getImage());
                cart.setAmount(dish.getPrice());

            }
            else  if (cart.getSetmealId() != null)
            {
                LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Setmeal::getId, cart.getSetmealId());
                Setmeal setmeal = setmealMapper.selectOne(queryWrapper);
                cart.setName(setmeal.getName());
                cart.setImage(setmeal.getImage());
                cart.setAmount(setmeal.getPrice());
            }
            cart.setNumber(1);
            cart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(cart);
        }
    }

    @Override
    public List<ShoppingCart> list() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        return shoppingCartMapper.selectList(queryWrapper);
    }

    @Override
    public void clean() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartMapper.delete(queryWrapper);
    }
}
