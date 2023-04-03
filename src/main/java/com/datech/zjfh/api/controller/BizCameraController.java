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
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.entity.BizLineEntity;
import com.datech.zjfh.api.entity.BizPresetEntity;
import com.datech.zjfh.api.service.*;
import com.datech.zjfh.api.util.BeanCopierUtil;
import com.datech.zjfh.api.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/biz")
@Slf4j
public class BizCameraController {

    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private SysOrgServiceImpl sysOrgService;
    @Resource
    private BizLineServiceImpl bizLineService;
    @Resource
    private BizIvsServiceImpl bizIvsService;
    @Resource
    private BizPresetServiceImpl bizPresetService;

    @GetMapping(value = "/camera/init")
    public Result<Object> cameraInit(Integer ivsId) {
        log.info("------------  camera init in , ivsId:{} ...---------", ivsId);
        try {
            bizCameraService.syncIvs1800Camera(bizIvsService.getById(ivsId));
            return Result.OK();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    @GetMapping("/camera/tree")
    public Result<Object> tree(Integer lineId) {
        log.info("------------ camera org tree in, lineId : {}---------", lineId);
        try {
            SysOrgTreeVo root = sysOrgService.tree();
            List<Integer> ivsIdList = null;
            if (lineId != null) {
                ivsIdList = bizIvsService.getIvsList(lineId);
            }else {
                ivsIdList = bizIvsService.getAllIvsList();
            }
            if (CollectionUtils.isNotEmpty(ivsIdList)) {
                bizCameraService.addArea(root, ivsIdList);
            }
            return Result.OK(root);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询组织节点数据失败，信息：{}", e.getMessage());
            return Result.error("查询组织节点数据失败");
        }
    }


    @GetMapping(value = "/camera/pageList")
    public Result<Object> pageList(String orgName, String cameraName, String deviceIp, Integer status, Integer subsEnable, Integer lineId,
                                   @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {

        List<Integer> ivsIdList = null;
        if (lineId != null) {
            ivsIdList = bizIvsService.getIvsList(lineId);
            if (CollectionUtils.isEmpty(ivsIdList)) {
                return Result.OK(new Page<>());
            }
        }
        LambdaQueryWrapper<BizCameraEntity> queryWrapper = Wrappers.lambdaQuery();
        if (CollectionUtils.isNotEmpty(ivsIdList)) {
            queryWrapper.in(BizCameraEntity::getIvsId, ivsIdList);
        }
        if (StringUtils.isNotBlank(orgName)) {
            List<Integer> orgIdList = sysOrgService.getOrgAndChildIdList(orgName);
            if (CollectionUtils.isNotEmpty(orgIdList)) {
                queryWrapper.in(BizCameraEntity::getOrgId, orgIdList);
            } else {
                queryWrapper.eq(BizCameraEntity::getArea, orgName);
            }
        }
        if (StringUtils.isNotBlank(cameraName)) {
            queryWrapper.likeLeft(BizCameraEntity::getName, cameraName);
        }
        if (StringUtils.isNotBlank(deviceIp)) {
            queryWrapper.eq(BizCameraEntity::getDeviceIp, deviceIp);
        }
        if (status != null) {
            queryWrapper.eq(BizCameraEntity::getStatus, status);
        }
        if (subsEnable != null) {
            queryWrapper.eq(BizCameraEntity::getSubsEnable, subsEnable);
        }
        queryWrapper.orderByDesc(BizCameraEntity::getCreateTime);
        Page<BizCameraEntity> entityPage = bizCameraService.page(new Page<>(pageNo, pageSize), queryWrapper);
        Page<BizCameraVo> voPage = convertVoPage(entityPage);
        return Result.OK(voPage);
    }

    private Page<BizCameraVo> convertVoPage(Page<BizCameraEntity> entityPage) {
        Page<BizCameraVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<BizCameraVo> voList = new ArrayList<>();
        if (entityPage.getRecords().size() > 0) {
            entityPage.getRecords().forEach(o -> {
                BizCameraVo vo = BeanCopierUtil.copyBean(o, BizCameraVo.class);
                vo.setOrgName(sysOrgService.getOrgFullName(o.getOrgId()));
                BizIvsEntity ivs = bizIvsService.getById(o.getIvsId());
                if (ivs != null) {
                    BizLineEntity line = bizLineService.getById(ivs.getLineId());
                    if (line != null) {
                        vo.setLineName(line.getName());
                    }
                }
                voList.add(vo);
            });
        }
        voPage.setRecords(voList);
        return voPage;
    }



    /**
     * 查询未绑定组织节点的摄像头
     */
    @GetMapping(value = "/camera/unbind")
    public Result<Object> getCameraUnbind() {
        LambdaQueryWrapper<BizCameraEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.isNull(BizCameraEntity::getOrgId);
        List<BizCameraEntity> entityList = bizCameraService.list(queryWrapper);
        List<BizCameraCodeVo> voList = new ArrayList<>();
        if (entityList.size() > 0) {
            entityList.forEach(o -> {
                BizCameraCodeVo vo = BeanCopierUtil.copyBean(o, BizCameraCodeVo.class);
                voList.add(vo);
            });
        }
        return Result.OK(voList);
    }

    /**
     * 区域设置批量
     */
    @PutMapping(value = "/camera/batch")
    public Result<Object> editBatch(@RequestBody BizCameraEditVo param) {
        log.info("------------  camera edit in :{}", JSONObject.toJSONString(param));
        if (param == null || param.getOrgId() == null || param.getAlarmType() == null
                || CollectionUtils.isEmpty(param.getAreaVoList())) {
            return Result.error("必填参数为空");
        }
        if (param.getOrgId() < 1 || sysOrgService.getById(param.getOrgId()) == null) {
            return Result.error("组织节点为空");
        }
        try {
            for (BizCameraEditAreaVo vo : param.getAreaVoList()) {
                String[] coordinateArray = null;
                if (StringUtils.isNotBlank(vo.getCoordinates())) {
                    coordinateArray = vo.getCoordinates().split(";");
                }
                for (int i = 0; i < vo.getIdList().size(); i++) {
                    int id = vo.getIdList().get(i);
                    BizCameraEntity camera = bizCameraService.getById(id);
                    camera.setOrgId(param.getOrgId());
                    camera.setArea(vo.getArea());
                    if (coordinateArray != null && i < coordinateArray.length) {
                        String[] array = coordinateArray[i].split(",");
                        camera.setLongitude(array[0]);
                        if (array.length > 1) {
                            camera.setLatitude(array[1]);
                        }
                    }
                    //加载或删除预置位，修改告警类型之前调用此方法
                    editPreset(camera, param.getAlarmType());
                    camera.setAlarmType(param.getAlarmType());
                    bizCameraService.updateById(camera);
                }
            }
            return Result.OK();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    /**
     * 编辑
     */
    @PutMapping(value = "/camera/one")
    public Result<Object> editOne(@RequestBody BizCameraVo param) {
        log.info("------------  camera edit in :{}", JSONObject.toJSONString(param));
        if (param == null || param.getOrgId() == null || param.getId() == null || param.getAlarmType() == null) {
            return Result.error("必填参数为空");
        }
        if (param.getOrgId() < 1 || sysOrgService.getById(param.getOrgId()) == null) {
            return Result.error("组织节点为空");
        }
        try {
            BizCameraEntity entity = bizCameraService.getById(param.getId());
            if (entity == null) {
                return Result.error("操作失败，摄像头不存在");
            } else {
                entity.setOrgId(param.getOrgId());
                entity.setArea(param.getArea());
                entity.setName(param.getName());
                entity.setLongitude(param.getLongitude());
                entity.setLatitude(param.getLatitude());
                //加载或删除预置位，修改告警类型之前调用此方法
                editPreset(entity, param.getAlarmType());
                entity.setAlarmType(param.getAlarmType());
                if (bizCameraService.updateById(entity)) {
                    return Result.OK();
                } else {
                    return Result.error("操作保存失败");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }


    public void editPreset(BizCameraEntity camera, Integer updateAlarmType) {
        //告警类型，1：雷摄， 2：光摄
        if ((camera.getAlarmType() == null ||camera.getAlarmType() == 2) && updateAlarmType == 1) {
            //光摄改为雷摄，删除预置位
            LambdaQueryWrapper<BizPresetEntity> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(BizPresetEntity::getCameraIp, camera.getDeviceIp());
            bizPresetService.remove(queryWrapper);
        }
        if ((camera.getAlarmType() == null || camera.getAlarmType() == 1) && updateAlarmType == 2) {
            //雷摄改为光摄，加载预置位
            BizIvsEntity ivs = bizIvsService.getById(camera.getIvsId());
            bizPresetService.presetReload(camera, ivs);
        }
    }

    /**
     * 查询摄像头详情
     */
    @GetMapping(value = "/camera/{id}")
    public Result<Object> getCameraUnbind(@PathVariable("id") Integer id) {
        BizCameraEntity entity = bizCameraService.getById(id);
        BizCameraVo vo = new BizCameraVo();
        if (entity != null) {
            vo = BeanCopierUtil.copyBean(entity, BizCameraVo.class);
        }
        return Result.OK(vo);
    }


}
