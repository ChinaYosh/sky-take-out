package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
/**
 * 通用接口
 */
@ApiOperation("通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    @RequestMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile  file) throws IOException {

        log.info("文件上传：{}",file);

       String url = aliOssUtil.upload(file);
       log.info("文件上传成功：{}",url);
       if(url != null)
       return Result.success(url);
       return Result.error("上传失败");
    }
}
