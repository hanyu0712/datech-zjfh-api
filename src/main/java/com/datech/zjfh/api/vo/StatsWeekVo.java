package com.datech.zjfh.api.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StatsWeekVo {
    public StatsTotalVo total;  //总数
    public StatsPieVo pie;  //饼图
    public List<List<Map<String, Integer>>> bar;
}
