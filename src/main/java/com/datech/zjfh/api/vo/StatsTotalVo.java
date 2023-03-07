package com.datech.zjfh.api.vo;

import lombok.Data;

@Data
public class StatsTotalVo {
    public int totalLast = 0;  //昨天总数
    public int dealLast = 0;  //昨天已处理
    public int total = 0;  //今天总数
    public int deal = 0;  //今天已处理
    public int dealCorrect = 0;  //今日正确告警
    public int unDeal = 0;  //今日未处理
    public int ignore = 0;  //今日已忽略
}
