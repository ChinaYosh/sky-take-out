package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig
{
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory)
    {
        log.info("开始配置RedisTemplate");
       RedisTemplate redisTemplate = new RedisTemplate();
       // 设置RedisConnectionFactory
       redisTemplate.setConnectionFactory(redisConnectionFactory);
       log.info("RedisTemplate配置成功");
       redisTemplate.setKeySerializer(new StringRedisSerializer());
       redisTemplate.setHashKeySerializer(new StringRedisSerializer());
       //value用json
       redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
       redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
       return redisTemplate;
    }
}
