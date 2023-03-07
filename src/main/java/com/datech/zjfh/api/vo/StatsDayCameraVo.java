package com.datech.zjfh.api.vo;

import lombok.Data;

@Data
public class StatsDayCameraVo {
    public String lineName;
    public String cameraName;
    public String deviceIp;
    public String orgName;
    public int totalYesterday = 0;  //昨天总数
    public int total = 0;  //今天总数
    public int deal = 0;  //今天已处理
    public int unDeal = 0;  //今日未处理
    public int ignore = 0;  //今日已忽略
    public Integer alarmType;
}
