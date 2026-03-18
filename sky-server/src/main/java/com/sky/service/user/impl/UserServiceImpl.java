package com.sky.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.admin.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.user.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService
{
    public static  final  String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO)
    {
        String openid = getOpenid(userLoginDTO.getCode());
        if(openid == null)
        {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        User user = userMapper.getByOpenid(openid);
        if(user == null)
        {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        return user;
    }
    private String getOpenid(String code)
    {
        Map<String ,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        Map<String,Object> parse = JSON.parseObject(json);
        if(parse.get("openid") == null)
        {
            String errMsg = parse.get("errmsg") != null ? parse.get("errmsg").toString() : "获取 openid 失败";
            throw new LoginFailedException("微信登录失败：" + errMsg);
        }
        return parse.get("openid").toString();
    }

}
