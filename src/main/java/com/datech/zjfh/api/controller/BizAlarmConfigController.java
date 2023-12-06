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
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.common.consts.LogConstant;
import com.datech.zjfh.api.entity.BizAlarmConfigEntity;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.query.BizAlarmConfigQuery;
import com.datech.zjfh.api.query.BizCameraQuery;
import com.datech.zjfh.api.service.BizAlarmConfigServiceImpl;
import com.datech.zjfh.api.service.BizCameraServiceImpl;
import com.datech.zjfh.api.service.BizIvsServiceImpl;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.LoginUtil;
import com.datech.zjfh.api.util.RedisUtil;
import com.datech.zjfh.api.vo.BizAlarmConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/biz")
@Slf4j
public class BizAlarmConfigController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private BizAlarmConfigServiceImpl bizAlarmConfigService;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizIvsServiceImpl bizIvsService;


    @GetMapping(value = "/alarm/subscribe/config/{id}")
    public Result<Object> pageList(@PathVariable("id") Integer id) {
        if (id == null) {
            return Result.error("必填参数为空");
        }
        LambdaQueryWrapper<BizAlarmConfigEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BizAlarmConfigEntity::getCameraId, id);
        queryWrapper.eq(BizAlarmConfigEntity::getState, 0);
        List<BizAlarmConfigEntity> configList = bizAlarmConfigService.list(queryWrapper);
        List<BizAlarmConfigVo> voList = new ArrayList<>();
        configList.forEach(config -> {
            BizAlarmConfigVo vo = new BizAlarmConfigVo();
            vo.setBeginTime(config.getBeginTime());
            vo.setEndTime(config.getEndTime());
            voList.add(vo);
        });
        return Result.OK(voList);
    }


    /**
     * 设置非布防时段
     */
    @PutMapping(value = "/camera/subscribe/config")
    public Result<Object> cameraSubscribeConfig(@RequestBody BizAlarmConfigQuery param) {
        log.info("------------  camera subscribe config in :{}", JSONObject.toJSONString(param));
        try {
            return bizAlarmConfigService.add(param);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    /**
     * 批量设置非布防时段
     */
    @PutMapping(value = "/camera/subscribe/configBatch")
    public Result<Object> cameraSubscribeConfigBatch(@RequestBody BizAlarmConfigQuery param) {
        log.info("------------  camera subscribe config Batch in :{}", JSONObject.toJSONString(param));
        try {
            return bizAlarmConfigService.addBatch(param);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }


    /**
     * 打开布防
     */
    @PutMapping(value = "/camera/subscribe/open")
    public Result<Object> cameraSubscribeOpen(@RequestBody BizCameraQuery param) {
        log.info("------------  camera subscribe open in :{}", JSONObject.toJSONString(param));
        try {
            //操作开启布防，要检查摄像头是否有配置非布防时间
            LambdaQueryWrapper<BizAlarmConfigEntity> configQuery = Wrappers.lambdaQuery();
            configQuery.in(BizAlarmConfigEntity::getCameraId, param.getIdList());
            configQuery.eq(BizAlarmConfigEntity::getState, 0);
            List <BizAlarmConfigEntity> configList = bizAlarmConfigService.list(configQuery);
            //当前时间
            String nowTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
            for (BizAlarmConfigEntity config : configList){
                if ((nowTime.compareTo(config.getBeginTime()) >= 0 && nowTime.compareTo(config.getEndTime()) < 0)) {
                    return Result.error("操作失败，当前时间为非布防时间段");
                }
            }
            return doCameraSubscribe(param, 1);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    /**
     * 关闭布防
     */
    @PutMapping(value = "/camera/subscribe/close")
    public Result<Object> cameraSubscribeClose(@RequestBody BizCameraQuery param) {
        log.info("------------  camera subscribe close in :{}", JSONObject.toJSONString(param));
        try {
            return doCameraSubscribe(param, 0);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }


    public Result<Object> doCameraSubscribe(BizCameraQuery param, int subsEnable) {
        if (param == null || CollectionUtils.isEmpty(param.getIdList())) {
            return Result.error("参数为空");
        }
        StringBuilder nameBuild = new StringBuilder();
        List<BizCameraEntity> successList = new ArrayList<>();
        List<BizCameraEntity> errorList = new ArrayList<>();
        LambdaQueryWrapper<BizCameraEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(BizCameraEntity::getId, param.getIdList());
        List<BizCameraEntity> entityList = bizCameraService.list(queryWrapper);
        for (BizCameraEntity entity : entityList) {
            if (subsEnable == 0) {
                //关闭布防，删除订阅告警
                if (StringUtils.isNotBlank(entity.getSubscribeId())) {
                    int resultCode = bizCameraService.deleteIntelligentData(entity.getSubscribeId(), bizIvsService.getById(entity.getIvsId()));
                    if (resultCode == 0) {
                        nameBuild.append(entity.getName()).append(",");
                        entity.setSubscribeId("");
                        entity.setSubsEnable(0);
                        successList.add(entity);
                    } else {
                        errorList.add(entity);
                    }
                } else {
                    //冗余操作
                    entity.setSubsEnable(0);
                    successList.add(entity);
                }
            }
            if (subsEnable == 1) {
                //订阅告警
                int resultCode = bizCameraService.addIntelligentData(entity, bizIvsService.getById(entity.getIvsId()));
                if (resultCode == 0) {
                    nameBuild.append(entity.getName()).append(",");
                    entity.setSubsEnable(1);
                    successList.add(entity);
                } else {
                    errorList.add(entity);
                }
            }
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (successList.size() > 0) {
                if (bizCameraService.updateBatchById(successList)) {
                    logUtil.addLog("预警布防设置","摄像头:['" + nameBuild + "']布防状态修改为:" + (subsEnable == 0 ? "关闭" : "开启"), LogConstant.OPERATE_TYPE_3, loginUser);
                    if (errorList.size() == 0) {
                        return Result.OK();
                    } else {
                        return Result.error("部分设备操作失败，请检查设备情况后重新操作");
                    }
                } else {
                    return Result.error("数据库操作失败");
                }
            }
            return Result.error("操作失败，请检查设备情况后重新操作");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }


}
