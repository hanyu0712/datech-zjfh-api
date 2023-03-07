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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.entity.BizAlarmEntity;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.entity.BizLineEntity;
import com.datech.zjfh.api.service.*;
import com.datech.zjfh.api.vo.StatsDayAreaVo;
import com.datech.zjfh.api.vo.StatsDayCameraVo;
import com.datech.zjfh.api.vo.StatsTotalVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
public class StatsDayController {

    @Resource
    private BizAlarmServiceImpl bizAlarmService;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizLineServiceImpl bizLineService;
    @Resource
    private BizIvsServiceImpl bizIvsService;
    @Resource
    private SysOrgServiceImpl sysOrgService;
    @Resource
    private StatsServiceImpl statsService;


    @GetMapping(value = "/day/area")
    public Result<Object> dayArea(String day, Integer lineId, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("------------  stats day area in day:{}, lineId:{}", day, lineId);

        List<Integer> ivsIdList = bizIvsService.getIvsList(lineId);
        if (CollectionUtils.isEmpty(ivsIdList)) {
            return Result.OK(new Page<>());
        }
        QueryWrapper<BizCameraEntity> cameraQueryWrapper = Wrappers.<BizCameraEntity>query();
        cameraQueryWrapper.select(" DISTINCT area, org_id ");
        cameraQueryWrapper.in("ivs_id", ivsIdList);
        cameraQueryWrapper.isNotNull("area");
        Page<BizCameraEntity> cameraPage = bizCameraService.page(new Page<>(pageNo, pageSize), cameraQueryWrapper);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date paramDay = new Date();
        try {
            paramDay = format.parse(day + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(paramDay);
        // 今日0点
        Date todayBegin = cal.getTime();
        // 明日0点
        cal.add(Calendar.DATE, 1);
        Date tomorrowBegin = cal.getTime();
        // 昨日0点
        cal.add(Calendar.DATE, -2);
        Date yesterdayBegin = cal.getTime();

        Page<StatsDayAreaVo> voPage = new Page<>(cameraPage.getCurrent(), cameraPage.getSize(), cameraPage.getTotal());
        List<StatsDayAreaVo> voList = new ArrayList<>();
        if (cameraPage.getRecords().size() > 0) {
            for (BizCameraEntity camera : cameraPage.getRecords()) {
                StatsDayAreaVo vo = new StatsDayAreaVo();
                vo.setArea(camera.getArea());
                vo.setOrgName(sysOrgService.getOrgFullName(camera.getOrgId()));
                //查询摄像头编码列表
                LambdaQueryWrapper<BizCameraEntity> cameraWrapper = Wrappers.lambdaQuery(BizCameraEntity.class)
                        .select(BizCameraEntity::getCode)
                        .eq(BizCameraEntity::getArea, camera.getArea())
                        .in(lineId != null, BizCameraEntity::getIvsId, ivsIdList);
                List<String> cameraCodeList = bizCameraService.listObjs(cameraWrapper, Object::toString);
                if (CollectionUtils.isNotEmpty(cameraCodeList)) {

                    LambdaQueryWrapper<BizAlarmEntity> queryWrapper = statsService.getDayAreaQueryWrapper(yesterdayBegin, todayBegin, cameraCodeList, lineId);
                    int totalYesterday = bizAlarmService.count(queryWrapper);   //昨天总数

                    queryWrapper = statsService.getDayAreaQueryWrapper(todayBegin, tomorrowBegin, cameraCodeList, lineId);
                    int total = bizAlarmService.count(queryWrapper);   //今天总数

                    queryWrapper.eq(BizAlarmEntity::getState, 1);
                    int deal = bizAlarmService.count(queryWrapper); //今天已处理

                    queryWrapper = statsService.getDayAreaQueryWrapper(todayBegin, tomorrowBegin, cameraCodeList, lineId);
                    queryWrapper.eq(BizAlarmEntity::getState, 0);
                    int unDeal = bizAlarmService.count(queryWrapper);   //今日未处理

                    queryWrapper = statsService.getDayAreaQueryWrapper(todayBegin, tomorrowBegin, cameraCodeList, lineId);
                    queryWrapper.eq(BizAlarmEntity::getState, 2);
                    int ignore = bizAlarmService.count(queryWrapper);   //今日已忽略
                    vo.setDeal(deal);
                    vo.setUnDeal(unDeal);
                    vo.setIgnore(ignore);
                    vo.setTotal(total);
                    vo.setTotalYesterday(totalYesterday);
                }
                voList.add(vo);
            }
        }
        voPage.setRecords(voList);
        return Result.OK(voPage);
    }


    @GetMapping(value = "/day/camera")
    public Result<Object> dayCamera(String day, Integer lineId, Integer alarmType, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Integer> ivsIdList = bizIvsService.getIvsList(lineId);
        if (CollectionUtils.isEmpty(ivsIdList)) {
            return Result.OK(new Page<>());
        }
        LambdaQueryWrapper<BizCameraEntity> cameraQueryWrapper = Wrappers.lambdaQuery();
        cameraQueryWrapper.in(BizCameraEntity::getIvsId, ivsIdList);
        if (alarmType != null) {
            cameraQueryWrapper.eq(BizCameraEntity::getAlarmType, alarmType);
        }
        Page<BizCameraEntity> entityPage = bizCameraService.page(new Page<>(pageNo, pageSize), cameraQueryWrapper);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date paramDay = new Date();
        try {
            paramDay = format.parse(day + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(paramDay);
        // 今日0点
        Date todayBegin = cal.getTime();
        // 明日0点
        cal.add(Calendar.DATE, 1);
        Date tomorrowBegin = cal.getTime();
        // 昨日0点
        cal.add(Calendar.DATE, -2);
        Date yesterdayBegin = cal.getTime();
        Page<StatsDayCameraVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<StatsDayCameraVo> voList = new ArrayList<>();
        if (entityPage.getRecords().size() > 0) {
            for (BizCameraEntity camera : entityPage.getRecords()) {
                LambdaQueryWrapper<BizAlarmEntity> queryWrapper = getDayCodeQueryWrapper(yesterdayBegin, todayBegin, camera.getCode(), lineId, alarmType);
                int totalYesterday = bizAlarmService.count(queryWrapper);   //昨天总数

                queryWrapper = getDayCodeQueryWrapper(todayBegin, tomorrowBegin, camera.getCode(), lineId, alarmType);
                int total = bizAlarmService.count(queryWrapper);   //今天总数

                queryWrapper.eq(BizAlarmEntity::getState, 1);
                int deal = bizAlarmService.count(queryWrapper); //今天已处理

                queryWrapper = getDayCodeQueryWrapper(todayBegin, tomorrowBegin, camera.getCode(), lineId, alarmType);
                queryWrapper.eq(BizAlarmEntity::getState, 0);
                int unDeal = bizAlarmService.count(queryWrapper);   //今日未处理

                queryWrapper = getDayCodeQueryWrapper(todayBegin, tomorrowBegin, camera.getCode(), lineId, alarmType);
                queryWrapper.eq(BizAlarmEntity::getState, 2);
                int ignore = bizAlarmService.count(queryWrapper);   //今日已忽略

                StatsDayCameraVo vo = new StatsDayCameraVo();
                vo.setDeal(deal);
                vo.setUnDeal(unDeal);
                vo.setIgnore(ignore);
                vo.setTotal(total);
                vo.setTotalYesterday(totalYesterday);
                vo.setCameraName(camera.getName());
                vo.setDeviceIp(camera.getDeviceIp());
                vo.setOrgName(sysOrgService.getOrgFullName(camera.getOrgId()));
                BizIvsEntity ivs = bizIvsService.getById(camera.getIvsId());
                BizLineEntity line = bizLineService.getById(ivs.getLineId());
                if (line != null) {
                    vo.setLineName(line.getName());
                }
                vo.setAlarmType(camera.getAlarmType());
                voList.add(vo);
            }
        }
        voPage.setRecords(voList);
        return Result.OK(voPage);
    }


    @GetMapping(value = "/day/total")
    public Result<Object> dayTotal(String day, Integer lineId) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date todayBegin = new Date();
        try {
            todayBegin = format.parse(day + " 00:00:00");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(todayBegin);
        //  明天0点
        cal.add(Calendar.DATE, 1);
        Date tomorrowBegin = cal.getTime();
        //  昨天0点
        cal.add(Calendar.DATE, -2);
        Date yesterdayBegin = cal.getTime();

        StatsTotalVo total = statsService.getTotal(yesterdayBegin, todayBegin, todayBegin, tomorrowBegin, lineId);
        return Result.OK(total);
    }

    private LambdaQueryWrapper<BizAlarmEntity> getDayCodeQueryWrapper(Date begin, Date end, String code, Integer lineId, Integer alarmType) {
        LambdaQueryWrapper<BizAlarmEntity> queryWrapper = statsService.getDateQueryWrapper(begin, end, lineId);
        queryWrapper.eq(BizAlarmEntity::getCameraCode, code);
        if (alarmType != null) {
            queryWrapper.eq(BizAlarmEntity::getAlarmType, alarmType);
        }
        return queryWrapper;
    }




}
