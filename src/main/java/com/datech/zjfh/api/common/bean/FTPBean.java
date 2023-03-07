package com.datech.zjfh.api.common.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ftp")
public class FTPBean {
    private String hostname; // 机器的ip
    private int port; // 端口号：21
    private String username; // 账号
    private String password; // 密码
    private String basePath; // 文件的位置
    private String remotePath; // 文件的位置
    private String fileName; // 文件的位置
    private String version; // 版本
}
