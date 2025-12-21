package com.example.documentmanager.config;

import com.example.documentmanager.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.annotation.PostConstruct;
import javax.servlet.MultipartConfigElement;

@Configuration
public class FileStorageConfig {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        // 确保上传目录存在
        FileUtil.createDirIfNotExists(uploadDir);
    }

    /**
     * 配置文件上传大小限制（无限制）
     * 用于支持大文件上传测试
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 设置单个文件最大大小（设置为10TB，实际相当于无限制）
        factory.setMaxFileSize(DataSize.ofTerabytes(10));
        // 设置总上传数据大小（设置为10TB，实际相当于无限制）
        factory.setMaxRequestSize(DataSize.ofTerabytes(10));
        return factory.createMultipartConfig();
    }
}