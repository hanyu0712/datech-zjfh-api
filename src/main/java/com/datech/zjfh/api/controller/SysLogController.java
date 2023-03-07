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
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.entity.SysLogEntity;
import com.datech.zjfh.api.query.SysLogQuery;
import com.datech.zjfh.api.service.SysLogServiceImpl;
import com.datech.zjfh.api.service.SysUserServiceImpl;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 提供用户数据接口
 */
@RestController
@RequestMapping("/sys")
@Slf4j
public class SysLogController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private SysLogServiceImpl sysLogService;

    @GetMapping(value = "/log/pageList")
    public Result<Object> pageList(String name, String deviceIp, String beginTime, String endTime, @RequestParam(defaultValue = "1") Integer pageNo,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<SysLogEntity> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.eq(SysLogEntity::getOpUsername, name);
        }
        if (StringUtils.isNotBlank(deviceIp)) {
            queryWrapper.eq(SysLogEntity::getDeviceIp, deviceIp);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (StringUtils.isNotBlank(beginTime)) {
                queryWrapper.ge(SysLogEntity::getCreateTime, format.parse(beginTime + " 00:00:00"));
            }
            if (StringUtils.isNotBlank(endTime)) {
                queryWrapper.le(SysLogEntity::getCreateTime, format.parse(endTime + " 23:59:59"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        queryWrapper.orderByDesc(SysLogEntity::getCreateTime);
        Page<SysLogEntity> entityPage = sysLogService.page(new Page<>(pageNo, pageSize), queryWrapper);
        return Result.OK(entityPage);
    }


}
