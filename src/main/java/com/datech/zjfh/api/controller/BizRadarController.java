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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.common.consts.LogConstant;
import com.datech.zjfh.api.entity.*;
import com.datech.zjfh.api.service.*;
import com.datech.zjfh.api.util.BeanCopierUtil;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.LoginUtil;
import com.datech.zjfh.api.util.RedisUtil;
import com.datech.zjfh.api.vo.BizRadarBatch;
import com.datech.zjfh.api.vo.BizRadarVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/biz")
@Slf4j
public class BizRadarController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private BizRadarServiceImpl bizRadarService;
    @Resource
    private BizLineServiceImpl bizLineService;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizIvsServiceImpl bizIvsService;
    @Resource
    private SysOrgServiceImpl sysOrgService;


    @GetMapping(value = "/radar/pageList")
    public Result<Object> pageList(Integer lineId, String ip, @RequestParam(defaultValue = "1") Integer pageNo,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        List<String> cameraIpList = null;
        if (lineId != null){
            List<Integer> ivsIdList = bizIvsService.getIvsList(lineId);
            if (CollectionUtils.isNotEmpty(ivsIdList)) {
                LambdaQueryWrapper<BizCameraEntity> cameraWrapper = Wrappers.lambdaQuery();
                cameraWrapper.in(BizCameraEntity::getIvsId, ivsIdList);
                cameraWrapper.select(BizCameraEntity::getDeviceIp);
                cameraWrapper.eq(BizCameraEntity::getAlarmType, 1); //雷达摄像头
                cameraIpList = bizCameraService.listObjs(cameraWrapper, Object::toString);
            }
            if (CollectionUtils.isEmpty(cameraIpList)) {
                return Result.OK(new Page<>());
            }
        }
        LambdaQueryWrapper<BizRadarEntity> queryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isNotEmpty(cameraIpList)) {
            queryWrapper.in(BizRadarEntity::getCameraIp, cameraIpList);
        }
        if (StringUtils.isNotBlank(ip)) {
            queryWrapper.eq(BizRadarEntity::getIp, ip);
        }
        queryWrapper.orderByAsc(BizRadarEntity::getId);
        Page<BizRadarEntity> entityPage = bizRadarService.page(new Page<>(pageNo, pageSize), queryWrapper);
        Page<BizRadarVo> voPage = convertVoPage(entityPage);
        return Result.OK(voPage);
    }

    private Page<BizRadarVo> convertVoPage(Page<BizRadarEntity> entityPage) {
        Page<BizRadarVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<BizRadarVo> voList = new ArrayList<>();
        if (entityPage.getRecords().size() > 0) {
            for (BizRadarEntity radar : entityPage.getRecords()) {
                BizRadarVo vo = BeanCopierUtil.copyBean(radar, BizRadarVo.class);
                LambdaQueryWrapper<BizCameraEntity> cameraWrapper = Wrappers.lambdaQuery();
                cameraWrapper.eq(BizCameraEntity::getDeviceIp, radar.getCameraIp());
                BizCameraEntity camera = bizCameraService.getOne(cameraWrapper);
                if (camera != null) {
                    vo.setOrgName(sysOrgService.getOrgFullName(camera.getOrgId()) + "-" + camera.getArea());
                    BizIvsEntity ivs = bizIvsService.getById(camera.getIvsId());
                    if (ivs != null) {
                        vo.setIvsName(ivs.getName());
                        BizLineEntity line = bizLineService.getById(ivs.getLineId());
                        if (line != null) {
                            vo.setLineName(line.getName());
                        }
                    }
                }
                voList.add(vo);
            }
        }
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 新增
     */
    @PostMapping(value = "/radar")
    public Result<Object> saveNce(@RequestBody BizRadarBatch readrBatch) {
        log.info("------------  radar save in :{}---------", JSONObject.toJSONString(readrBatch));
        if (readrBatch == null || CollectionUtils.isEmpty(readrBatch.getRadarList())) {
            return Result.error("参数为空");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            for (BizRadarEntity radar : readrBatch.getRadarList()) {
                bizRadarService.save(radar);
                logUtil.addLog("雷达管理", "雷达[" + radar.getName() + "]创建成功！", LogConstant.OPERATE_TYPE_2, loginUser);
            }
            return Result.OK(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    /**
     * 修改
     */
    @PutMapping(value = "/radar")
    public Result<Object> editNce(@RequestBody BizRadarEntity entity) {
        log.info("------------  radar edit in :{}---------", JSONObject.toJSONString(entity));
        if (entity == null || entity.getId() == null
                || StringUtils.isBlank(entity.getName()) || entity.getType() == null
                || StringUtils.isBlank(entity.getIp()) || StringUtils.isBlank(entity.getCameraIp())
                || StringUtils.isBlank(entity.getAccount()) || StringUtils.isBlank(entity.getPassword())) {
            return Result.error("参数为空");
        }
        BizRadarEntity entityOri = bizRadarService.getById(entity.getId());
        if (entityOri == null) {
            return Result.error(" 操作失败，雷达原始信息不存在");
        }
        try {
            if (bizRadarService.updateById(entity)) {
                return Result.OK(true);
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/radar/{id}")
    public Result<Object> removeNce(@PathVariable("id") Integer id) {
        log.info("------------  radar remove in :{}---------", id);
        if (id == null) {
            return Result.error("参数为空");
        }
        BizRadarEntity entity = bizRadarService.getById(id);
        if (entity == null) {
            return Result.error("雷达为空");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (bizRadarService.removeById(id)) {
                logUtil.addLog("雷达管理", "雷达[" + entity.getName() + "]删除成功！", LogConstant.OPERATE_TYPE_4, loginUser);
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
