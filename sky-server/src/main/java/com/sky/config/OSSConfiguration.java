package com.sky.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class OSSConfiguration
{
    @Autowired
    AliOssProperties aliOssProperties;
    @Bean
    public OSS ossClient( )
    {
        // 校验配置参数是否为空，缺失则终止启动
        if (StringUtils.isEmpty(aliOssProperties.getEndpoint()) ||
                StringUtils.isEmpty(aliOssProperties.getAccessKeyId()) ||
                StringUtils.isEmpty(aliOssProperties.getAccessKeySecret()) ||
                StringUtils.isEmpty(aliOssProperties.getBucketName()))
            throw new RuntimeException("请配置阿里云文件上传信息");
            return new OSSClientBuilder().build(aliOssProperties.getEndpoint(), aliOssProperties.getAccessKeyId(), aliOssProperties.getAccessKeySecret());
    }

}
