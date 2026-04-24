package com.sky.mapper.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User>
{
    @Select("select * from user where openid = #{openid}")
    public User getByOpenid(String openid);

    int insert(User user);

    @Select("select count(*) from user ${ew.customSqlSegment}")
    Long count(@Param("ew") LambdaQueryWrapper<User> queryWrapper);
}
