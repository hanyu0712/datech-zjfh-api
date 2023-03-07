package com.datech.zjfh.api.common.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "client")
public class ClientVersionBean {
    private String url; // 文件的位置
    private String version; // 版本
    private Integer isForce; // 强制
}
