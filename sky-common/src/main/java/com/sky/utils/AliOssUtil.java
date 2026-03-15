package com.sky.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.sky.properties.AliOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Component
@Slf4j
public class AliOssUtil {

    @Autowired
    private AliOssProperties aliOssProperties;
    
    private OSS ossClient;
    
    private OSS getOSSClient() {
        if (ossClient == null) {
            ossClient = new OSSClientBuilder().build(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret()
            );
        }
        return ossClient;
    }

    public String upload(MultipartFile file) {
        try {
            // 1. 校验文件是否为空
            if (file.isEmpty()) {
                throw new RuntimeException("上传文件不能为空");
            }

            // 2. 获取文件基础信息
            String originalFileName = file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();
            long fileSize = file.getSize();

            // 3. 生成 OSS 唯一文件名（UUID+ 原后缀，防止同名覆盖）
            String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + suffix;
            String ossFullPath = "image/" + uniqueFileName;

            // 4. 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSize);

            // 5. 执行上传
            PutObjectRequest request = new PutObjectRequest(
                aliOssProperties.getBucketName(), 
                ossFullPath, 
                inputStream, 
                metadata
            );
            getOSSClient().putObject(request);

            // 6. 生成完整访问 URL
            String bucketName = aliOssProperties.getBucketName();
            String endpoint = aliOssProperties.getEndpoint();
            String ossFileUrl = "https://" + bucketName + "." + endpoint.replace("https://", "").replace("http://", "") + "/" + ossFullPath;
            
            log.info("文件上传 OSS 成功！存储路径：{}，访问 URL：{}", ossFullPath, ossFileUrl);
            return ossFileUrl;

        } catch (Exception e) {
            log.error("文件上传 OSS 失败", e);
            throw new RuntimeException("文件上传 OSS 失败：" + e.getMessage());
        }
    }
}
