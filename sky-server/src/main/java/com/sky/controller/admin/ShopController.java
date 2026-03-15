package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController
{
    @Autowired
    private RedisTemplate redisTemplate;

    public  static  final  String KEY = "SHOP_STATUS";
    @PutMapping("/{status}")
    public Result settStatus(@PathVariable Integer status)
    {
        log.info("设置营业状态：{}",status==1?"营业中":"打烊中");
        // ✅ 将 Integer 转换为 String 再存储，避免 ClassCastException
        redisTemplate.opsForValue().set(KEY, status.toString());
        return Result.success();
    }
    @GetMapping("/status")
    public Result<Integer> getStatus()
    {
        // ✅ 先获取 String，再转换为 Integer
        String statusStr = (String) redisTemplate.opsForValue().get(KEY);
        Integer status = statusStr != null ? Integer.valueOf(statusStr) : null;
        log.info("获取营业状态：{}",status==1?"营业中":"打烊中");
        return Result.success(status);
    }
}
