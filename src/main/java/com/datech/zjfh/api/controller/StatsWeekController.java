/*
 *
 * Copyright (C) 1999-2016 IFLYTEK Inc.All Rights Reserved.
 *
 * History:
 * Version    Author          Date              Operation
 *   1.0	  onion   2018年6月25日上午10:23:00	      Create
 */

package com.datech.zjfh.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.entity.BizAlarmEntity;
import com.datech.zjfh.api.service.BizAlarmServiceImpl;
import com.datech.zjfh.api.service.StatsServiceImpl;
import com.datech.zjfh.api.vo.StatsPieVo;
import com.datech.zjfh.api.vo.StatsTotalVo;
import com.datech.zjfh.api.vo.StatsWeekVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 数据分析处理类
 */
@RestController
@RequestMapping("/stats")
@Slf4j
public class StatsWeekController {

    @Resource
    private BizAlarmServiceImpl bizAlarmService;
    @Resource
    private StatsServiceImpl statsService;


    @GetMapping(value = "/week/total")
    public Result<Object> dayTotal(String day, Integer lineId) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date paramDay = new Date();
        try {
            paramDay = format.parse(day + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(paramDay);
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        // 下周一0点
        if (dayWeek == 1) {
            //根据需求，周日要当做本周最后一天，需要特殊处理
            cal.add(Calendar.DATE, 1);
        }else {
            cal.add(Calendar.DATE, 7 - (dayWeek - 2));
        }
        Date nextWeekBegin = cal.getTime();
        // 本周一0点
        cal.add(Calendar.DATE, -7);
        Date weekBegin = cal.getTime();
        // 上周一0点
        cal.add(Calendar.DATE, -7);
        Date lastWeekBegin = cal.getTime();

        StatsWeekVo weekVo = new StatsWeekVo();
        StatsTotalVo total = statsService.getTotal(lastWeekBegin, weekBegin, weekBegin, nextWeekBegin, lineId);
        weekVo.setTotal(total);

        StatsPieVo pieVo = new StatsPieVo();
        pieVo.setLevel1(100);
        weekVo.setPie(pieVo);

        SimpleDateFormat formatDay = new SimpleDateFormat("MM-dd");
        List<List<Map<String, Integer>>> bar = new ArrayList<>();
        List<Map<String, Integer>> rader = new ArrayList<>();
        List<Map<String, Integer>> ray = new ArrayList<>();
        bar.add(rader);
        bar.add(ray);
        cal = Calendar.getInstance();
        cal.setTime(weekBegin);
        for (int i = 0; i < 7; i++) {
            Date begin = cal.getTime();
            cal.add(Calendar.DATE, 1);
            Date end = cal.getTime();
            LambdaQueryWrapper<BizAlarmEntity> queryWrapper = statsService.getDateQueryWrapper(begin, end, lineId);
            queryWrapper.eq(BizAlarmEntity::getAlarmType, 1);//雷摄
            Map<String, Integer> map = new HashMap<>();
            map.put(formatDay.format(begin), bizAlarmService.count(queryWrapper));
            rader.add(map);
            queryWrapper = statsService.getDateQueryWrapper(begin, end, lineId);
            queryWrapper.eq(BizAlarmEntity::getAlarmType, 2);//光摄
            map = new HashMap<>();
            map.put(formatDay.format(begin), bizAlarmService.count(queryWrapper));
            ray.add(map);
        }
        weekVo.setBar(bar);
        return Result.OK(weekVo);
    }



}
