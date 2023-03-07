/*
 *
 * Copyright (C) 1999-2016 IFLYTEK Inc.All Rights Reserved.
 *
 * History:
 * Version    Author          Date              Operation
 *   1.0	  onion   2018年6月25日上午10:23:00	      Create
 */

package com.datech.zjfh.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.common.consts.LogConstant;
import com.datech.zjfh.api.common.consts.WebConstant;
import com.datech.zjfh.api.entity.BizCameraAlarmEntity;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.query.BizAlarmDealQuery;
import com.datech.zjfh.api.service.BizCameraAlarmServiceImpl;
import com.datech.zjfh.api.service.BizCameraServiceImpl;
import com.datech.zjfh.api.service.SysOrgServiceImpl;
import com.datech.zjfh.api.util.*;
import com.datech.zjfh.api.vo.BizCameraAlarmVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/biz")
@Slf4j
public class BizCameraAlarmController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizCameraAlarmServiceImpl bizCameraAlarmService;
    @Resource
    private SysOrgServiceImpl sysOrgService;


    @GetMapping(value = "/cameraAlarm/pageList")
    public Result<Object> pageList(Integer orgId, String area, String name, String deviceIp, Integer state,
                                   String beginTime, String endTime, @RequestParam(defaultValue = "1") Integer pageNo,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        if (orgId == null || StringUtils.isBlank(area)) {
            return Result.error("必填参数为空");
        }
        LambdaQueryWrapper<BizCameraEntity> cameraWrapper = Wrappers.lambdaQuery();
        cameraWrapper.select(BizCameraEntity::getId);
        cameraWrapper.eq(BizCameraEntity::getArea, area);
        cameraWrapper.eq(BizCameraEntity::getOrgId, orgId);
        if (StringUtils.isNotBlank(name)) {
            cameraWrapper.eq(BizCameraEntity::getName, name);
        }
        if (StringUtils.isNotBlank(deviceIp)) {
            cameraWrapper.eq(BizCameraEntity::getDeviceIp, deviceIp);
        }
        List<Object> cameraIdList = bizCameraService.listObjs(cameraWrapper);
        if (CollectionUtils.isEmpty(cameraIdList)) {
            cameraIdList.add(-1);
        }
        LambdaQueryWrapper<BizCameraAlarmEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(BizCameraAlarmEntity::getCameraId, cameraIdList);
        if (state != null) {
            queryWrapper.eq(BizCameraAlarmEntity::getState, state);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (StringUtils.isNotBlank(beginTime)) {
                queryWrapper.ge(BizCameraAlarmEntity::getCreateTime, format.parse(beginTime + " 00:00:00"));
            }
            if (StringUtils.isNotBlank(endTime)) {
                queryWrapper.le(BizCameraAlarmEntity::getCreateTime, format.parse(endTime + " 23:59:59"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        queryWrapper.orderByDesc(BizCameraAlarmEntity::getCreateTime);

        Page<BizCameraAlarmEntity> entityPage = bizCameraAlarmService.page(new Page<>(pageNo, pageSize), queryWrapper);
        Page<BizCameraAlarmVo> voPage = convertVoPage(entityPage);
        return Result.OK(voPage);
    }

    private Page<BizCameraAlarmVo> convertVoPage(Page<BizCameraAlarmEntity> entityPage) {
        Page<BizCameraAlarmVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<BizCameraAlarmVo> voList = new ArrayList<>();
        if (entityPage.getRecords().size() > 0) {
            Map<Integer, BizCameraEntity> map = new HashMap<>();
            entityPage.getRecords().forEach(o -> {
                BizCameraAlarmVo vo = BeanCopierUtil.copyBean(o, BizCameraAlarmVo.class);
                BizCameraEntity camera = map.get(vo.getCameraId());
                if (camera == null) {
                    camera = bizCameraService.getById(vo.getCameraId());
                    map.put(vo.getCameraId(), camera);
                }
                if (camera != null) {
                    vo.setOrgId(camera.getOrgId());
                    vo.setOrgName(sysOrgService.getOrgFullName(o.getOrgId()));
                    vo.setArea(camera.getArea());
                    vo.setCameraName(camera.getName());
                    vo.setCameraCode(camera.getCode());
                    vo.setCameraIp(vo.getCameraIp());
                }
                voList.add(vo);
            });
        }
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 忽略
     */
    @PutMapping(value = "/cameraAlarm/ignore")
    public Result<Object> cameraAlarmIgnore(@RequestBody BizAlarmDealQuery deal) {
        log.info("------------  camera alarm ignore in ...---------");
        return commonDeal(deal, 2);
    }
    /**
     * 处理
     */
    @PutMapping(value = "/cameraAlarm/deal")
    public Result<Object> cameraAlarmDeal(@RequestBody BizAlarmDealQuery deal) {
        log.info("------------  camera alarm deal in ...---------");
        return commonDeal(deal, 1);
    }
    private Result<Object> commonDeal(BizAlarmDealQuery deal, int state) {
        if (deal == null || deal.getIdList() == null || deal.getIdList().size() == 0) {
            return Result.error("参数为空");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            LambdaUpdateWrapper<BizCameraAlarmEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(BizCameraAlarmEntity::getId, deal.getIdList());
            BizCameraAlarmEntity entity = new BizCameraAlarmEntity();
            entity.setOpinions(deal.getOpinions());
            entity.setState(state);
            entity.setClearTime(new Date());
            if (loginUser != null) {
                entity.setClearUser(loginUser.getRealname());
            }
            if (bizCameraAlarmService.update(entity, updateWrapper)) {
                logUtil.addLog("设备告警","设备告警处理成功！", LogConstant.OPERATE_TYPE_3, loginUser);
                return Result.OK(true);
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }




}
