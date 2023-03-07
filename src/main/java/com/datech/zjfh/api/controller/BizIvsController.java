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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.common.consts.LogConstant;
import com.datech.zjfh.api.common.ivs.Login;
import com.datech.zjfh.api.entity.*;
import com.datech.zjfh.api.service.*;
import com.datech.zjfh.api.util.BeanCopierUtil;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.LoginUtil;
import com.datech.zjfh.api.util.RedisUtil;
import com.datech.zjfh.api.vo.BizCameraVo;
import com.datech.zjfh.api.vo.BizIvsVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/biz")
@Slf4j
public class BizIvsController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private BizIvsServiceImpl bizIvsService;
    @Resource
    private BizLineServiceImpl bizLineService;
    @Resource
    private BizPresetServiceImpl bizPresetService;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizAlarmConfigServiceImpl bizAlarmConfigService;
    @Resource
    private BizRadarServiceImpl bizRadarService;


    @GetMapping(value = "/ivs/pageList")
    public Result<Object> pageList(Integer lineId, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<BizIvsEntity> queryWrapper = Wrappers.lambdaQuery();
        if (lineId != null) {
            queryWrapper.eq(BizIvsEntity::getLineId, lineId);
        }
        queryWrapper.orderByAsc(BizIvsEntity::getId);
        Page<BizIvsEntity> entityPage = bizIvsService.page(new Page<>(pageNo, pageSize), queryWrapper);
        Page<BizIvsVo> voPage = convertVoPage(entityPage);
        return Result.OK(voPage);
    }

    private Page<BizIvsVo> convertVoPage(Page<BizIvsEntity> entityPage) {
        Page<BizIvsVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<BizIvsVo> voList = new ArrayList<>();
        if (entityPage.getRecords().size() > 0) {
            for (BizIvsEntity ivs : entityPage.getRecords()) {
                BizIvsVo vo = BeanCopierUtil.copyBean(ivs, BizIvsVo.class);
                BizLineEntity line = bizLineService.getById(ivs.getLineId());
                if (line != null) {
                    vo.setLineName(line.getName());
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
    @PostMapping(value = "/ivs")
    public Result<Object> saveNce(@RequestBody BizIvsEntity entity) {
        log.info("------------  ivs save in :{}---------", entity);
        if (entity == null || StringUtils.isBlank(entity.getIp()) || entity.getLineId() == null
                || StringUtils.isBlank(entity.getName()) || StringUtils.isBlank(entity.getAccount())
                || StringUtils.isBlank(entity.getPassword())) {
            return Result.error("参数为空");
        }
        try {
            //登录
            String token = Login.loginAndGetToken("https://" + entity.getIp() + ":18531", entity.getAccount(), entity.getPassword());
            log.info("------------  ivs save, request token:{}", token);
            if (StringUtils.isBlank(token)) {
                return Result.error("IVS连接失败");
            }
            entity.setToken(token); //订阅告警需要token
            entity.setOnLine(0);
            bizIvsService.save(entity);
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            logUtil.addLog("IVS管理", "IVS[" + entity.getName() + "]创建成功！", LogConstant.OPERATE_TYPE_2, loginUser);
            //初始化摄像头,订阅告警
            bizCameraService.syncIvs1800Camera(entity);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
        return Result.OK(true);
    }

    /**
     * 修改
     */
    @PutMapping(value = "/ivs")
    public Result<Object> editIvs(@RequestBody BizIvsEntity entity) {
        log.info("------------  ivs edit in :{}---------", entity);
        if (entity == null || StringUtils.isBlank(entity.getName()) || entity.getId() == null) {
            return Result.error("参数为空");
        }
        BizIvsEntity entityOri = bizIvsService.getById(entity.getId());
        if (entityOri == null) {
            return Result.error(" IVS为空");
        }
        entityOri.setName(entity.getName());
        try {
            if (bizIvsService.updateById(entityOri)) {
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
    @DeleteMapping(value = "/ivs/{id}")
    public Result<Object> removeIvs(@PathVariable("id") Integer id) {
        log.info("------------  ivs remove in :{}---------", id);
        if (id == null) {
            return Result.error("参数为空");
        }
        BizIvsEntity entity = bizIvsService.getById(id);
        if (entity == null) {
            return Result.error("IVS为空");
        }
        try {
            List<String> ipList = bizCameraService.listObjs(Wrappers.lambdaQuery(BizCameraEntity.class).select(BizCameraEntity::getDeviceIp).eq(BizCameraEntity::getIvsId, id), Object::toString);
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (CollectionUtils.isNotEmpty(ipList)) {
                //删除预置位
                LambdaQueryWrapper<BizPresetEntity> presetWrapper = Wrappers.lambdaQuery();
                presetWrapper.in(BizPresetEntity::getCameraIp, ipList);
                bizPresetService.remove(presetWrapper);
                //删除雷达
                LambdaQueryWrapper<BizRadarEntity> radarWrapper = Wrappers.lambdaQuery();
                radarWrapper.in(BizRadarEntity::getCameraIp, ipList);
                bizRadarService.remove(radarWrapper);
                //删除非布防时段配置
                String realName = loginUser == null ? null : loginUser.getRealname();
                bizAlarmConfigService.deleteByCameraIp(ipList, realName);
                //删除摄像头
                LambdaQueryWrapper<BizCameraEntity> cameraWrapper = Wrappers.lambdaQuery();
                cameraWrapper.in(BizCameraEntity::getIvsId, id);
                bizCameraService.remove(cameraWrapper);
                logUtil.addLog("摄像头管理", "删除IVS[" + entity.getName() + "]，同步删除摄像头！", LogConstant.OPERATE_TYPE_4, loginUser);
            }
            bizIvsService.removeById(id);
            logUtil.addLog("IVS管理", "IVS[" + entity.getName() + "]删除成功！", LogConstant.OPERATE_TYPE_4, loginUser);
            return Result.OK(true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }

    }

    /**
     * 查询ivs详情
     */
    @GetMapping(value = "/ivs/detail")
    public Result<Object> getCameraUnbind(String cameraIp) {
        LambdaQueryWrapper<BizCameraEntity> cameraWrapper = Wrappers.lambdaQuery();
        cameraWrapper.in(BizCameraEntity::getDeviceIp, cameraIp);
        BizCameraEntity camera = bizCameraService.getOne(cameraWrapper);
        BizIvsEntity entity = bizIvsService.getById(camera.getIvsId());
        return Result.OK(entity);
    }
}
