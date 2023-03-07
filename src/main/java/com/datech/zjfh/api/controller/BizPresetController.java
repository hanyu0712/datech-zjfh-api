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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.common.ivs.Ptzcontrol;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.entity.BizPresetEntity;
import com.datech.zjfh.api.service.BizCameraServiceImpl;
import com.datech.zjfh.api.service.BizIvsServiceImpl;
import com.datech.zjfh.api.service.BizPresetServiceImpl;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.RedisUtil;
import com.datech.zjfh.api.vo.BizPresetEditVo;
import com.datech.zjfh.api.vo.BizPresetUnbindVo;
import com.datech.zjfh.api.vo.BizPresetVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/biz")
@Slf4j
public class BizPresetController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizPresetServiceImpl bizPresetService;
    @Resource
    private BizIvsServiceImpl bizIvsService;

    /**
     * 重新加载预置位
     */
    @PutMapping(value = "/preset/reload")
    public Result<Object> presetReload(@RequestBody BizPresetEditVo vo) {
        log.info("------------  preset reload in :{}---------", JSONObject.toJSONString(vo));
        if (vo == null || vo.getLineId() == null) {
            return Result.error(" 参数为空");
        }
        LambdaQueryWrapper<BizIvsEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BizIvsEntity::getLineId, vo.getLineId());
        List<BizIvsEntity> ivsList = bizIvsService.list(queryWrapper);
        try {
            for (BizIvsEntity ivs : ivsList) {
                LambdaQueryWrapper<BizCameraEntity> cameraWrapper = Wrappers.lambdaQuery();
                cameraWrapper.eq(BizCameraEntity::getIvsId, ivs.getId());
                List<BizCameraEntity> cameraList = bizCameraService.list(cameraWrapper);
                for (BizCameraEntity camera : cameraList) {
                    bizPresetService.presetReload(camera, ivs);
                }
            }
            return Result.OK();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    /**
     * 绑定Nce
     */
    @PutMapping(value = "/preset/bind")
    public Result<Object> bindNce(@RequestBody BizPresetEditVo vo) {
        log.info("------------  preset bind nce in :{}---------", JSONObject.toJSONString(vo));
        if (vo == null || vo.getNceId() == null || StringUtils.isBlank(vo.getIp()) || vo.getPresetMap() == null) {
            return Result.error(" 参数为空");
        }
        LambdaQueryWrapper<BizPresetEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BizPresetEntity::getCameraIp, vo.getIp());
        List<BizPresetEntity> presetList = bizPresetService.list(queryWrapper);
        if (CollectionUtils.isEmpty(presetList)) {
            return Result.error("预置位为空");
        }
        try {
            Map<Integer, float[]> presetMap = vo.getPresetMap();
            for (BizPresetEntity preset : presetList) {
                if (vo.getPresetMap().containsKey(preset.getPresetIndex())) {
                    preset.setNceId(vo.getNceId());
                    float[] beginEnd = presetMap.get(preset.getPresetIndex());
                    if (beginEnd != null && beginEnd.length == 2) {
                        preset.setBegin(beginEnd[0]);
                        preset.setEnd(beginEnd[1]);
                    }
                    bizPresetService.updateById(preset);
                }
            }
            return Result.OK("操作成功");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    /**
     * 解绑Nce
     */
    @PutMapping(value = "/preset/unbind")
    public Result<Object> unbindNce(@RequestBody BizPresetEditVo vo) {
        log.info("------------  preset unbind nce in :{}---------", JSONObject.toJSONString(vo));
        if (StringUtils.isBlank(vo.getIp())) {
            return Result.error(" 参数为空");
        }
        try {
            UpdateWrapper<BizPresetEntity> updateWrapper = new UpdateWrapper<>();
            //可将指定字段更新为null
            updateWrapper.set("begin", 0f);
            updateWrapper.set("end", 0f);
            updateWrapper.set("nce_id", null);
            updateWrapper.eq("camera_ip", vo.getIp());
            bizPresetService.update(updateWrapper);
            return Result.OK("操作成功");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    @PutMapping(value = "/preset/ptccontrol")
    public Result<Object> ptccontrol(Integer presetId) {
        try {
            BizPresetEntity preset = bizPresetService.getById(presetId);
            BizIvsEntity ivs = bizIvsService.getById(1);
            if (ivs != null) {
                String ptzResult = Ptzcontrol.sendRequest("https://" + ivs.getIp() + ":18531", ivs.getToken(), preset.getCameraCode(), preset.getPresetIndex());
                log.info("调用云台控制结果：{}", ptzResult);
                return Result.OK(ptzResult);
            }
            return Result.error("没有ivs");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    /**
     * 查询未绑定Nce的预置位
     */
    @GetMapping(value = "/preset/unbindList")
    public Result<Object> presetList(Integer lineId) {
        log.info("------------ preset unbind list in, lineId : {}---------", lineId);
        if (lineId == null) {
            return Result.error("参数为空");
        }
        BizPresetUnbindVo result = new BizPresetUnbindVo();
        List<Integer> ivsIdList = bizIvsService.getIvsList(lineId);
        if (CollectionUtils.isNotEmpty(ivsIdList)) {
            LambdaQueryWrapper<BizCameraEntity> cameraWrapper = Wrappers.lambdaQuery(BizCameraEntity.class)
                    .select(BizCameraEntity::getDeviceIp)
                    .in(BizCameraEntity::getIvsId, ivsIdList)
                    .eq(BizCameraEntity::getAlarmType, 2); //光纤
            List<String> cameraIpList = bizCameraService.listObjs(cameraWrapper, Object::toString);
            if (CollectionUtils.isNotEmpty(cameraIpList)) {
                LambdaQueryWrapper<BizPresetEntity> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.isNull(BizPresetEntity::getNceId);
                queryWrapper.in(BizPresetEntity::getCameraIp, cameraIpList);
                List<BizPresetEntity> presetList = bizPresetService.list(queryWrapper);
                if (CollectionUtils.isNotEmpty(presetList)) {
                    result.setIpList(presetList.stream().map(BizPresetEntity::getCameraIp).collect(Collectors.toSet()));
                    Map<String, List<Integer>> presetMap = new HashMap<>();
                    result.getIpList().forEach(ip -> {
                        presetMap.put(ip, presetList.stream().filter(s -> ip.equals(s.getCameraIp())).map(BizPresetEntity::getPresetIndex).collect(Collectors.toList()));
                    });
                    result.setPresetMap(presetMap);
                }
            }
        }
        return Result.OK(result);
    }


    /**
     * 查询已绑定Nce的预置位
     *
     * @param nceId
     */
    @GetMapping(value = "/preset/bindList")
    public Result<Object> bindList(Integer nceId, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        if (nceId == null) {
            return Result.error("参数为空");
        }
        QueryWrapper<BizPresetEntity> query = new QueryWrapper<>();
        query.select(" DISTINCT camera_ip ");
        query.eq("nce_id", nceId);
        query.orderByAsc("camera_ip ");
        Page<BizPresetEntity> entityPage = bizPresetService.page(new Page<>(pageNo, pageSize), query);
        Page<BizPresetVo> voPage = convertVoPage(entityPage);

        return Result.OK(voPage);
    }

    private Page<BizPresetVo> convertVoPage(Page<BizPresetEntity> entityPage) {
        Page<BizPresetVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<BizPresetVo> voList = new ArrayList<>();
        if (entityPage.getRecords().size() > 0) {
            for (BizPresetEntity presetIp : entityPage.getRecords()) {
                LambdaQueryWrapper<BizPresetEntity> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.eq(BizPresetEntity::getCameraIp, presetIp.getCameraIp());
                List<BizPresetEntity> presetList = bizPresetService.list(queryWrapper);
                BizPresetVo vo = null;
                for (BizPresetEntity preset : presetList) {
                    if (vo == null) {
                        vo = new BizPresetVo();
                        vo.setIp(preset.getCameraIp());
                        vo.setName(preset.getCameraName());
                        Map<Integer, float[]> beginEndMap = new HashMap<>();
                        vo.setBeginEndMap(beginEndMap);
                    }
                    vo.setPresetCount(vo.getPresetCount() + 1);
                    if (preset.getBegin() != null && preset.getBegin() < vo.getBegin()) {
                        vo.setBegin(preset.getBegin());
                    }
                    if (preset.getEnd() != null && preset.getEnd() > vo.getEnd()) {
                        vo.setEnd(preset.getEnd());
                    }
                    float[] array = new float[2];
                    array[0] = preset.getBegin() == null ? -1 : preset.getBegin();
                    array[1] = preset.getEnd() == null ? -1 : preset.getEnd();
                    vo.getBeginEndMap().put(preset.getPresetIndex(), array);
                }
                voList.add(vo);
            }
        }
        voPage.setRecords(voList);
        return voPage;
    }


}
