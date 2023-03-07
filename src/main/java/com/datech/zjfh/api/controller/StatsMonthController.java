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
import com.datech.zjfh.api.entity.BizAlarmEntity;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.service.*;
import com.datech.zjfh.api.vo.StatsMonthVo;
import com.datech.zjfh.api.vo.StatsPieVo;
import com.datech.zjfh.api.vo.StatsTotalVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
public class StatsMonthController {

    @Resource
    private BizAlarmServiceImpl bizAlarmService;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizIvsServiceImpl bizIvsService;
    @Resource
    private SysOrgServiceImpl sysOrgService;
    @Resource
    private StatsServiceImpl statsService;


    @GetMapping(value = "/month/total")
    public Result<Object> dayTotal(String day, Integer lineId) {
        StatsMonthVo monthVo = new StatsMonthVo();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date paramDay = new Date();
            paramDay = format.parse(day + " 00:00:00");

            Calendar cal = Calendar.getInstance();
            cal.setTime(paramDay);
            int dayMonth = cal.get(Calendar.DAY_OF_MONTH);
            // 本月1日0点
            cal.add(Calendar.DATE, 1 - dayMonth);
            Date monthBegin = cal.getTime();
            // 下月1日0点
            cal.add(Calendar.MONTH, 1);
            Date nextMonthBegin = cal.getTime();
            // 上月1日0点
            cal.add(Calendar.MONTH, -2);
            Date lastMonthBegin = cal.getTime();

            StatsTotalVo total = statsService.getTotal(lastMonthBegin, monthBegin, monthBegin, nextMonthBegin, lineId);
            monthVo.setTotal(total);

            StatsPieVo pieVo = new StatsPieVo();
            pieVo.setLevel1(100);
            monthVo.setPie(pieVo);

            SimpleDateFormat formatDay = new SimpleDateFormat("MM-dd");
            List<List<Map<String, Integer>>> bar = new ArrayList<>();
            List<List<Map<String, Integer>>> dealBar = new ArrayList<>();
            List<List<Map<String, Integer>>> ignoreBar = new ArrayList<>();
            List<Map<String, Integer>> raderBar = new ArrayList<>();
            List<Map<String, Integer>> rayBar = new ArrayList<>();
            bar.add(raderBar);
            bar.add(rayBar);
            List<Map<String, Integer>> raderDealBar = new ArrayList<>();
            List<Map<String, Integer>> rayDealBar = new ArrayList<>();
            dealBar.add(raderDealBar);
            dealBar.add(rayDealBar);
            List<Map<String, Integer>> raderIgnoreBar = new ArrayList<>();
            List<Map<String, Integer>> rayIgnoreBar = new ArrayList<>();
            ignoreBar.add(raderIgnoreBar);
            ignoreBar.add(rayIgnoreBar);
            monthVo.setBar(bar);
            monthVo.setDealBar(dealBar);
            monthVo.setIgnoreBar(ignoreBar);

            Calendar paramCalendar = Calendar.getInstance();
            paramCalendar.setTime(paramDay);
            Calendar forCalendar = Calendar.getInstance();
            forCalendar.setTime(monthBegin);
            for (int i = 0; i < paramCalendar.get(Calendar.DAY_OF_MONTH); i++) {
                Date begin = forCalendar.getTime();
                forCalendar.add(Calendar.DATE, 1);
                Date end = forCalendar.getTime();
                //告警数量
                LambdaQueryWrapper<BizAlarmEntity> queryWrapper = statsService.getDateQueryWrapper(begin, end, lineId);
                queryWrapper.eq(BizAlarmEntity::getAlarmType, 1);//雷摄
                Map<String, Integer> map = new HashMap<>();
                map.put(formatDay.format(begin), bizAlarmService.count(queryWrapper));
                raderBar.add(map);
                queryWrapper = statsService.getDateQueryWrapper(begin, end, lineId);
                queryWrapper.eq(BizAlarmEntity::getAlarmType, 2);//光摄
                map = new HashMap<>();
                map.put(formatDay.format(begin), bizAlarmService.count(queryWrapper));
                rayBar.add(map);
                //处理数量
                queryWrapper = statsService.getDateQueryWrapper(begin, end, lineId);
                queryWrapper.eq(BizAlarmEntity::getState, 1);
                queryWrapper.eq(BizAlarmEntity::getAlarmType, 1);//雷摄
                map = new HashMap<>();
                map.put(formatDay.format(begin), bizAlarmService.count(queryWrapper));
                raderDealBar.add(map);
                queryWrapper = statsService.getDateQueryWrapper(begin, end, lineId);
                queryWrapper.eq(BizAlarmEntity::getState, 1);
                queryWrapper.eq(BizAlarmEntity::getAlarmType, 2);//光摄
                map = new HashMap<>();
                map.put(formatDay.format(begin), bizAlarmService.count(queryWrapper));
                rayDealBar.add(map);
                //忽略数量
                queryWrapper = statsService.getDateQueryWrapper(begin, end, lineId);
                queryWrapper.eq(BizAlarmEntity::getState, 2);
                queryWrapper.eq(BizAlarmEntity::getAlarmType, 1);//雷摄
                map = new HashMap<>();
                map.put(formatDay.format(begin), bizAlarmService.count(queryWrapper));
                raderIgnoreBar.add(map);
                queryWrapper = statsService.getDateQueryWrapper(begin, end, lineId);
                queryWrapper.eq(BizAlarmEntity::getState, 2);
                queryWrapper.eq(BizAlarmEntity::getAlarmType, 2);//光摄
                map = new HashMap<>();
                map.put(formatDay.format(begin), bizAlarmService.count(queryWrapper));
                rayIgnoreBar.add(map);
            }


            Map<String, Integer> areaMap = new HashMap<>();
            QueryWrapper<BizCameraEntity> areaWrapper = Wrappers.<BizCameraEntity>query().select(" DISTINCT area, org_id ");
            if (lineId != null) {
                List<Integer> ivsIdList = bizIvsService.getIvsList(lineId);
                if (CollectionUtils.isNotEmpty(ivsIdList)) {
                    areaWrapper.in("ivs_id", ivsIdList);
                }
            }
            List<BizCameraEntity> cameraList = bizCameraService.list(areaWrapper);
            for (BizCameraEntity camera : cameraList) {
                if (camera.getOrgId() != null) {
                    LambdaQueryWrapper<BizCameraEntity> cameraWrapper = Wrappers.lambdaQuery(BizCameraEntity.class)
                            .select(BizCameraEntity::getCode)
                            .eq(BizCameraEntity::getOrgId, camera.getOrgId())
                            .eq(BizCameraEntity::getArea, camera.getArea());
                    if (lineId != null) {
                        List<Integer> ivsIdList = bizIvsService.getIvsList(lineId);
                        if (CollectionUtils.isNotEmpty(ivsIdList)) {
                            cameraWrapper.in(BizCameraEntity::getIvsId, ivsIdList);
                        }
                    }
                    List<String> cameraCodeList = (List<String>) bizCameraService.listObjs(cameraWrapper, Object::toString);
                    if (CollectionUtils.isEmpty(cameraCodeList)) {
                        areaMap.put(sysOrgService.getOrgFullName(camera.getOrgId()) + camera.getArea(), 0);
                    } else {
                        LambdaQueryWrapper<BizAlarmEntity> queryWrapper = statsService.getDayAreaQueryWrapper(monthBegin, nextMonthBegin, cameraCodeList, lineId);
                        if (lineId != null) {
                            queryWrapper.eq(BizAlarmEntity::getLineId, lineId);
                        }
                        int totalArea = bizAlarmService.count(queryWrapper);   //本月总数
                        areaMap.put(sysOrgService.getOrgFullName(camera.getOrgId()) + camera.getArea(), totalArea);
                    }
                }
            }
            monthVo.setAreaMap(areaMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.OK(monthVo);
    }


}
