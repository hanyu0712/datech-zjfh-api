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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.common.consts.LogConstant;
import com.datech.zjfh.api.common.consts.WebConstant;
import com.datech.zjfh.api.entity.SysConfigEntity;
import com.datech.zjfh.api.service.SysConfigServiceImpl;
import com.datech.zjfh.api.util.JwtUtil;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.LoginUtil;
import com.datech.zjfh.api.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/sys")
@Slf4j
public class SysConfigController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private SysConfigServiceImpl sysConfigService;


    /**
     * 查询入侵告警处理意见模板
     */
    @GetMapping("/config/template")
    public Result<Object> getTemplate() {
        log.info("------------  get config template in ...---------");
        try {
            List<String> templateList = sysConfigService.listObjs(Wrappers.lambdaQuery(SysConfigEntity.class)
                    .select(SysConfigEntity::getItemValue).eq(SysConfigEntity::getItemCode, "template"), Object::toString);
            return Result.OK(templateList);
        } catch (Exception e) {
            log.error("查询config异常，信息：{}", e.getMessage());
            return Result.error("查询配置数据异常");
        }
    }
    /**
     * 查询系统配置
     */
    @GetMapping("/config/pageList")
    public Result<Object> getConfigs(@RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("------------  get config lists in ...---------");
        LambdaQueryWrapper<SysConfigEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysConfigEntity::getItemCode, "template");
        try {
            Page<SysConfigEntity> entityPage = sysConfigService.page(new Page<>(pageNo, pageSize), queryWrapper);
            return Result.OK(entityPage);
        } catch (Exception e) {
            log.error("查询config异常，信息：{}", e.getMessage());
            return Result.error("查询配置数据异常");
        }
    }

    /**
     * 新增配置信息
     */
    @PostMapping(value = "/config")
    public Result<Object> saveConfig(@RequestBody SysConfigEntity entity) {
        log.info("------------  config save in ...---------");
        if (entity == null || StringUtils.isBlank(entity.getItemCode())) {
            return Result.error("参数为空");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            entity.setId(null);
            if (sysConfigService.save(entity)) {
                logUtil.addLog("处理意见模板管理","系统配置[code：'" + entity.getItemCode() + "'value:'" + entity.getItemValue() + "']创建成功！", LogConstant.OPERATE_TYPE_2, loginUser);
                return Result.OK(true);
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error("新增配置信息异常：{}", e.getMessage());
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    /**
     * 删除
     */
    @DeleteMapping(value = "/config/{id}")
    public Result<Object> removeConfig(@PathVariable("id") Integer id) {
        log.info("------------  config remove in ...---------");
        if (id == null) {
            return Result.error("参数为空");
        }
        SysConfigEntity entity = sysConfigService.getById(id);
        if (entity == null) {
            return Result.error("配置信息为空");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (sysConfigService.removeById(id)) {
                logUtil.addLog("处理意见模板管理","系统配置[code：'" + entity.getItemCode() + "'value:'" + entity.getItemValue() + "']删除成功！", LogConstant.OPERATE_TYPE_4, loginUser);
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
     * 修改
     */
    @PutMapping(value = "/config")
    public Result<Object> editConfig(@RequestBody SysConfigEntity entityNew) {
        log.info("------------  config edit in ...---------");
        if (entityNew == null || entityNew.getId() == null || StringUtils.isBlank(entityNew.getItemCode())) {
            return Result.error("参数为空");
        }
        LambdaQueryWrapper<SysConfigEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysConfigEntity::getId, entityNew.getId());
        queryWrapper.eq(SysConfigEntity::getItemCode, entityNew.getItemCode());
        SysConfigEntity entityOri = sysConfigService.getOne(queryWrapper);
        if (entityOri == null) {
            return Result.error("配置信息为空");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (sysConfigService.updateById(entityNew)) {
                logUtil.addLog("处理意见模板管理","系统配置['" + entityOri.getItemCode() + "-'" + entityOri.getItemValue() + "'修改为'" + entityNew.getItemCode() + "-'" + entityNew.getItemValue() + "']修改成功！", LogConstant.OPERATE_TYPE_3, loginUser);
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
