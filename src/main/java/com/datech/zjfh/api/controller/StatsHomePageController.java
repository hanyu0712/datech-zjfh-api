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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.entity.*;
import com.datech.zjfh.api.service.*;
import com.datech.zjfh.api.vo.StatsAlarmStateVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/stats")
@Slf4j
public class StatsHomePageController {

    @Resource
    private BizAlarmServiceImpl bizAlarmService;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizLineServiceImpl bizLineService;
    @Resource
    private BizIvsServiceImpl bizIvsService;
    @Resource
    private StatsServiceImpl statsService;

    @GetMapping(value = "/homePage/alarm")
    public Result<Object> homePage(Integer lineId) {
        Map<String, Object> result = new HashMap<>();
        int cameraOnNum = 0;
        int cameraOffNum = 0;
        List<Integer> ivsIdList = bizIvsService.getIvsList(lineId);
        if (CollectionUtils.isNotEmpty(ivsIdList)) {
            //摄像头数量
            LambdaQueryWrapper<BizCameraEntity> queryCamera = Wrappers.lambdaQuery();
            if (lineId != null) {
                queryCamera.in(BizCameraEntity::getIvsId, ivsIdList);
            }
            queryCamera.ne(BizCameraEntity::getStatus, 0);
            cameraOnNum = bizCameraService.count(queryCamera);
            queryCamera = Wrappers.lambdaQuery();
            if (lineId != null) {
                queryCamera.in(BizCameraEntity::getIvsId, ivsIdList);
            }
            queryCamera.eq(BizCameraEntity::getStatus, 0);
            cameraOffNum = bizCameraService.count(queryCamera);
        }
        int cameraTotal = cameraOnNum + cameraOffNum;
        //检查区域数量
        QueryWrapper<BizCameraEntity> queryCamera2 = new QueryWrapper<>();
        queryCamera2.select(" DISTINCT area ");
        if (lineId != null) {
            queryCamera2.eq("line_id", lineId);
        }
        int areaNum = bizCameraService.count(queryCamera2);
        //今日告警数量
//        log.info("----------------今日告警数量---------------");
        Calendar today = Calendar.getInstance();
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MILLISECOND, 0);
        LambdaQueryWrapper<BizAlarmEntity> queryWrapper = Wrappers.lambdaQuery();
        if (lineId != null) {
            queryWrapper.eq(BizAlarmEntity::getLineId, lineId);
        }
        queryWrapper.ge(true, BizAlarmEntity::getTriggerTime, today.getTime());
        int alarmTodayNum = bizAlarmService.count(queryWrapper);
        //本周告警数量，从周一0点开始统计
//        log.info("---------------本周告警数量---------------");
        //这里为什么要减1呢？因为一周的第一天是周日，第二天是周一。
        //如果我们date传的刚好是周日，比如2020-12-06，那么获取12月6号所在的周的周一就是12月7号
        //所以我们减1，取周六12-05，再取周一，就是我们所认识的所在周的周一就是12-01号了
        Calendar monday = Calendar.getInstance();
        monday.setTime(today.getTime());
        monday.add(Calendar.DATE, -1);
        //day of week 获取周中第几天，周一是第二天
        monday.set(Calendar.DAY_OF_WEEK, 2);
        queryWrapper = Wrappers.lambdaQuery();
        if (lineId != null) {
            queryWrapper.eq(BizAlarmEntity::getLineId, lineId);
        }
        queryWrapper.ge(true, BizAlarmEntity::getTriggerTime, monday.getTime());
        int alarmWeekNum = bizAlarmService.count(queryWrapper);
//        log.info("---------------一周每天告警数量---------------");
        SimpleDateFormat formatDay = new SimpleDateFormat("MM-dd");
        List<List<Map<String, Integer>>> dayStatsArrayList = new ArrayList<>();
        List<Map<String, Integer>> rader = new ArrayList<>();
        List<Map<String, Integer>> ray = new ArrayList<>();
        dayStatsArrayList.add(rader);
        dayStatsArrayList.add(ray);
        StatsAlarmStateVo weekStats = new StatsAlarmStateVo();
        for (int i = 0; i < 7; i++) {
            long beginMil = monday.getTimeInMillis() + 86400000 * i;
            Date begin = new Date();
            begin.setTime(beginMil);
            Calendar end = Calendar.getInstance();
            end.setTime(begin);
            end.set(Calendar.SECOND, 59);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.HOUR_OF_DAY, 23);
//            StatsAlarmStateVo dayStats = new StatsAlarmStateVo();
//            if (beginMil <= today.getTimeInMillis()) {
            queryWrapper = statsService.getDateQueryWrapper(begin, end.getTime(), lineId);
//                queryWrapper = Wrappers.lambdaQuery();
//                if (lineId != null) {
//                    queryWrapper.eq(BizAlarmEntity::getLineId, lineId);
//                }
//                queryWrapper.between(BizAlarmEntity::getTriggerTime, begin, end.getTime());
            List<BizAlarmEntity> alarmList = bizAlarmService.list(queryWrapper);
            alarmList.forEach(a -> {
                if (a.getState() == 0) weekStats.setNumState0(weekStats.getNumState0() + 1);
                if (a.getState() == 1) weekStats.setNumState1(weekStats.getNumState1() + 1);
                if (a.getState() == 2) weekStats.setNumState2(weekStats.getNumState2() + 1);
            });
//                weekStats.setNumState0(weekStats.getNumState0() + dayStats.getNumState0());
//                weekStats.setNumState1(weekStats.getNumState1() + dayStats.getNumState1());
//                weekStats.setNumState2(weekStats.getNumState2() + dayStats.getNumState2());
            queryWrapper.eq(BizAlarmEntity::getAlarmType, 1);//雷摄
            Map<String, Integer> map = new HashMap<>();
            map.put(formatDay.format(begin), bizAlarmService.count(queryWrapper));
            rader.add(map);
            queryWrapper = statsService.getDateQueryWrapper(begin, end.getTime(), lineId);
            queryWrapper.eq(BizAlarmEntity::getAlarmType, 2);//光摄
            map = new HashMap<>();
            map.put(formatDay.format(begin), bizAlarmService.count(queryWrapper));
            ray.add(map);
//            }
        }
        result.put("dayStats", dayStatsArrayList);
        result.put("weekStats", weekStats);
        result.put("cameraTotal", cameraTotal);
        result.put("cameraOnNum", cameraOnNum);
        result.put("cameraOffNum", cameraOffNum);
        result.put("areaNum", areaNum);
        result.put("alarmTodayNum", alarmTodayNum);
        result.put("alarmWeekNum", alarmWeekNum);
        return Result.OK(result);
    }

}
