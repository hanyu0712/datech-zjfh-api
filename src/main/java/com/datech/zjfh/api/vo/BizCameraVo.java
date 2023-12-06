package com.datech.zjfh.api.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BizCameraVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer orgId;
    private String code;
    private String domainCode;
    private String name;
    private String area;
    private String deviceIp;
    private String vendorType;
    private String orgName;
    private String longitude;
    private String latitude;
    private String lineName;
    private Integer status;
    private Integer subsEnable;
//    private Integer lineId;
    private Integer ivsId;
    private Integer alarmType;
}
