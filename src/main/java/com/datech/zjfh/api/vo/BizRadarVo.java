package com.datech.zjfh.api.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BizRadarVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Integer type;
    private String ip;
    private String account;
    private String password;
    private String cameraIp;
    private String lineName;
    private String ivsName;
    private String orgName;
}
