package com.datech.zjfh.api.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StatsMonthVo {
    public StatsTotalVo total;  //总数
    public StatsPieVo pie;  //饼图
    public List<List<Map<String, Integer>>> bar;
    public List<List<Map<String, Integer>>> dealBar;
    public List<List<Map<String, Integer>>> ignoreBar;
    public Map<String, Integer> areaMap;
}
