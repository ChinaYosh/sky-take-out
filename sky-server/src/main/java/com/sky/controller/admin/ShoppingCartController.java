package com.sky.controller.admin;

import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.admin.ShoppingCartService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@ApiOperation("购物车")
public class ShoppingCartController
{
    @Autowired
    private  ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result addShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO)
    {
        log.info("添加购物车,{}",shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }
    @GetMapping("/list")
    public Result list()
    {
        log.info("查询购物车");
        return Result.success(shoppingCartService.list());
    }
    @DeleteMapping("/clean")
    public Result clean()
    {
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }
}
