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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.common.consts.LogConstant;
import com.datech.zjfh.api.entity.BizAlarmEntity;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.entity.BizLineEntity;
import com.datech.zjfh.api.query.BizAlarmDealQuery;
import com.datech.zjfh.api.service.BizAlarmServiceImpl;
import com.datech.zjfh.api.service.BizCameraServiceImpl;
import com.datech.zjfh.api.service.BizLineServiceImpl;
import com.datech.zjfh.api.service.SysOrgServiceImpl;
import com.datech.zjfh.api.util.BeanCopierUtil;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.LoginUtil;
import com.datech.zjfh.api.util.RedisUtil;
import com.datech.zjfh.api.vo.BizAlarmVo;
import com.datech.zjfh.api.vo.BizCameraVo;
import lombok.extern.slf4j.Slf4j;
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
public class BizAlarmController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private BizAlarmServiceImpl bizAlarmService;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private SysOrgServiceImpl sysOrgService;
    @Resource
    private BizLineServiceImpl bizLineService;


    @GetMapping(value = "/alarm/today")
    public Result<Object> alarmToday() {
        List<Map<String, Object>> result = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MILLISECOND, 0);
        LambdaQueryWrapper<BizAlarmEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BizAlarmEntity::getAlarmType, 1);   //  雷摄
        queryWrapper.ge(BizAlarmEntity::getTriggerTime, today.getTime());
        result.add(getVoResult(bizAlarmService.list(queryWrapper)));
        queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(BizAlarmEntity::getAlarmType, 2);   //  光摄
        queryWrapper.ge(BizAlarmEntity::getTriggerTime, today.getTime());
        result.add(getVoResult(bizAlarmService.list(queryWrapper)));
        return Result.OK(result);
    }

    private Map<String, Object> getVoResult(List<BizAlarmEntity> alarmList) {
        List<BizAlarmVo> voList = new ArrayList<>();
        List<BizAlarmVo> voList0 = new ArrayList<>();
        List<BizAlarmVo> voList1 = new ArrayList<>();
        List<BizAlarmVo> voList2 = new ArrayList<>();
        Map<String, BizCameraVo> cameraVoMap = new HashMap<>();
        Map<Integer, String> lineNameMap = new HashMap<>();
        for(BizAlarmEntity alarm : alarmList){
            BizAlarmVo vo = BeanCopierUtil.copyBean(alarm, BizAlarmVo.class);
            vo.setCamera(getCameraVo(cameraVoMap, alarm.getCameraCode()));
            vo.setLineName(getLineName(lineNameMap, alarm.getLineId()));
            vo.setAlarmName("入侵告警");
            voList.add(vo);
            if (alarm.getState() == 0) voList0.add(vo);
            if (alarm.getState() == 1) voList1.add(vo);
            if (alarm.getState() == 2) voList2.add(vo);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("recordsTotal", voList);
        map.put("records0", voList0);
        map.put("records1", voList1);
        map.put("records2", voList2);
        map.put("numTotal", voList.size());
        map.put("numState0", voList0.size());
        map.put("numState1", voList1.size());
        map.put("numState2", voList2.size());
        return map;
    }


    @GetMapping(value = "/alarm/pageList")
    public Result<Object> pageList(Integer orgId, String area, String cameraCode, Integer state, String beginTime, String endTime, Integer alarmType,
                                   @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("------------  alarm pageList in ----------：orgId:{}, area:{}, cameraCode:{}, state:{}, alarmType:{}, beginTime:{}, endTime:{}, pageNo:{}, pageSize:{}",
                orgId, area, cameraCode, state, alarmType, beginTime, endTime, pageNo, pageSize);
        if (orgId == null || StringUtils.isBlank(area)) {
            return Result.error("必填参数为空");
        }

        List<String> cameraCodeList = new ArrayList<>();
        if (StringUtils.isBlank(cameraCode)) {
            cameraCodeList = bizCameraService.listObjs(Wrappers.lambdaQuery(BizCameraEntity.class).select(BizCameraEntity::getCode)
                    .eq(BizCameraEntity::getOrgId, orgId).eq(BizCameraEntity::getArea, area), Object::toString);
        } else {
            cameraCodeList.add(cameraCode);
        }
        if (cameraCodeList.size() == 0) {
            cameraCodeList.add("xx");//查不到摄像头编码，就不返回告警信息，添加xx使sql查不到数据
        }
        LambdaQueryWrapper<BizAlarmEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(BizAlarmEntity::getCameraCode, cameraCodeList);
        if (state != null) {
            queryWrapper.eq(BizAlarmEntity::getState, state);
        }
        if (alarmType != null) {
            queryWrapper.eq(BizAlarmEntity::getAlarmType, alarmType);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (StringUtils.isNotBlank(beginTime)) {
                queryWrapper.ge(BizAlarmEntity::getTriggerTime, format.parse(beginTime + " 00:00:00"));
            }
            if (StringUtils.isNotBlank(endTime)) {
                queryWrapper.le(BizAlarmEntity::getTriggerTime, format.parse(endTime + " 23:59:59"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        queryWrapper.orderByDesc(BizAlarmEntity::getTriggerTime);

        Page<BizAlarmEntity> entityPage = bizAlarmService.page(new Page<>(pageNo, pageSize), queryWrapper);
        Page<BizAlarmVo> voPage = convertVoPage(entityPage);
        return Result.OK(voPage);
    }

    private Page<BizAlarmVo> convertVoPage(Page<BizAlarmEntity> entityPage) {
        Page<BizAlarmVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<BizAlarmVo> voList = new ArrayList<>();
        if (entityPage.getRecords().size() > 0) {
            Map<String, BizCameraVo> cameraVoMap = new HashMap<>();
            Map<Integer, String> lineNameMap = new HashMap<>();
            entityPage.getRecords().forEach(o -> {
                BizAlarmVo vo = BeanCopierUtil.copyBean(o, BizAlarmVo.class);
                vo.setCamera(getCameraVo(cameraVoMap, o.getCameraCode()));
                vo.setLineName(getLineName(lineNameMap, o.getLineId()));
                vo.setAlarmName("入侵告警");
                vo.setLevel(1);
                voList.add(vo);
            });
        }
        voPage.setRecords(voList);
        return voPage;
    }

    private BizCameraVo getCameraVo(Map<String, BizCameraVo> cameraVoMap, String code) {
        if (StringUtils.isBlank(code))
            return null;
        BizCameraVo cameraVo = cameraVoMap.get(code);
        if (cameraVo == null) {
            LambdaQueryWrapper<BizCameraEntity> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(BizCameraEntity::getCode, code);
            BizCameraEntity camera = bizCameraService.getOne(queryWrapper);
            if (camera != null) {
                cameraVo = BeanCopierUtil.copyBean(camera, BizCameraVo.class);
                cameraVo.setOrgName(sysOrgService.getOrgFullName(camera.getOrgId()));
                cameraVoMap.put(code, cameraVo);
            }
        }
        return cameraVo;
    }

    private String getLineName(Map<Integer, String> lineNameMap, Integer lineId) {
        if (lineId == null)
            return null;
        String lineName = lineNameMap.get(lineId);
        if (StringUtils.isBlank(lineName)) {
            BizLineEntity line = bizLineService.getById(lineId);
            if (line != null) {
                lineNameMap.put(lineId, line.getName());
                lineName = line.getName();
            }
        }
        return lineName;
    }

    /**
     * 忽略
     */
    @PutMapping(value = "/alarm/ignore")
    public Result<Object> alarmIgnore(@RequestBody BizAlarmDealQuery deal) {
        log.info("------------  alarm ignore in ...---------");
        return commonDeal(deal, 2);
    }

    /**
     * 处理
     */
    @PutMapping(value = "/alarm/deal")
    public Result<Object> alarmDeal(@RequestBody BizAlarmDealQuery deal) {
        log.info("------------  alarm deal in ...---------");
        return commonDeal(deal, 1);
    }

    private Result<Object> commonDeal(BizAlarmDealQuery deal, int state) {
        if (deal == null || deal.getIdList() == null || deal.getIdList().size() == 0) {
            return Result.error("参数为空");
        }
        LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
        LambdaUpdateWrapper<BizAlarmEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(BizAlarmEntity::getId, deal.getIdList());
        BizAlarmEntity entity = new BizAlarmEntity();
        entity.setOpinions(deal.getOpinions());
        entity.setFalseAlarm(entity.getFalseAlarm() == null ? 0 : entity.getFalseAlarm());
        entity.setState(state);
        entity.setClearTime(new Date());
        if (loginUser != null) {
            entity.setClearUser(loginUser.getRealname());
        }
        try {
            if (bizAlarmService.update(entity, updateWrapper)) {
                logUtil.addLog("入侵预警","入侵告警信息操作成功！", LogConstant.OPERATE_TYPE_3, loginUser);
                return Result.OK(true);
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }


}
