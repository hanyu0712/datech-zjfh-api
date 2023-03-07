package com.datech.zjfh.api.vo;

import lombok.Data;

@Data
public class StatsAlarmStateVo {
    public int numState0 = 0;  //未处理
    public int numState1 = 0;  //已处理
    public int numState2 = 0;  //忽略
}
