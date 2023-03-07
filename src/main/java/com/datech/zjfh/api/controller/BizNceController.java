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
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.entity.BizLineEntity;
import com.datech.zjfh.api.entity.BizNceEntity;
import com.datech.zjfh.api.entity.BizPresetEntity;
import com.datech.zjfh.api.service.BizLineServiceImpl;
import com.datech.zjfh.api.service.BizNceServiceImpl;
import com.datech.zjfh.api.service.BizPresetServiceImpl;
import com.datech.zjfh.api.util.BeanCopierUtil;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.LoginUtil;
import com.datech.zjfh.api.util.RedisUtil;
import com.datech.zjfh.api.vo.BizNceVo;
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
public class BizNceController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private BizNceServiceImpl bizNceService;
    @Resource
    private BizLineServiceImpl bizLineService;
    @Resource
    private BizPresetServiceImpl bizPresetService;


    @GetMapping(value = "/nce/pageList")
    public Result<Object> pageList(Integer lineId, @RequestParam(defaultValue = "1") Integer pageNo,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<BizNceEntity> queryWrapper = Wrappers.lambdaQuery();
        if (lineId != null) {
            queryWrapper.eq(BizNceEntity::getLineId, lineId);
        }
        queryWrapper.orderByAsc(BizNceEntity::getId);
        Page<BizNceEntity> entityPage = bizNceService.page(new Page<>(pageNo, pageSize), queryWrapper);
        Page<BizNceVo> voPage = convertVoPage(entityPage);
        return Result.OK(voPage);
    }

    private Page<BizNceVo> convertVoPage(Page<BizNceEntity> entityPage) {
        Page<BizNceVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<BizNceVo> voList = new ArrayList<>();
        if (entityPage.getRecords().size() > 0) {
            for(BizNceEntity nce : entityPage.getRecords()){
                BizNceVo vo = BeanCopierUtil.copyBean(nce, BizNceVo.class);
                BizLineEntity line = bizLineService.getById(vo.getLineId());
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
    @PostMapping(value = "/nce")
    public Result<Object> saveNce(@RequestBody BizNceEntity entity) {
        log.info("------------  nce save in :{}---------", entity);
        if (entity == null || StringUtils.isBlank(entity.getIp()) || entity.getLineId() == null
                || StringUtils.isBlank(entity.getName()) || entity.getLength() == null
                || StringUtils.isBlank(entity.getAccount()) || StringUtils.isBlank(entity.getPassword())) {
            return Result.error("参数为空");
        }
        entity.setState(0);
        entity.setSubsEnable(0);
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (bizNceService.save(entity)) {
                logUtil.addLog("NCE管理","NCE[" + entity.getName() + "]创建成功！", LogConstant.OPERATE_TYPE_2, loginUser);
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
    @PutMapping(value = "/nce")
    public Result<Object> editNce(@RequestBody BizNceEntity entity) {
        log.info("------------  nce edit in :{}---------", entity);
        if (entity == null || StringUtils.isBlank(entity.getIp()) || entity.getLineId() == null
                || StringUtils.isBlank(entity.getName()) || entity.getLength() == null
                || StringUtils.isBlank(entity.getAccount()) || StringUtils.isBlank(entity.getPassword())) {
            return Result.error("参数为空");
        }
        BizNceEntity entityOri = bizNceService.getById(entity.getId());
        if (entityOri == null) {
            return Result.error(" NCE为空");
        }
        entity.setIdentifier(entityOri.getIdentifier());
        entity.setSession(entityOri.getSession());
        entity.setRoarand(entityOri.getRoarand());
        entity.setState(entityOri.getState());
        entity.setSubsEnable(entityOri.getSubsEnable());
        try {
            if (bizNceService.updateById(entity)) {
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
    @DeleteMapping(value = "/nce/{id}")
    public Result<Object> removeNce(@PathVariable("id") Integer id) {
        log.info("------------  nce remove in :{}---------", id);
        if (id == null) {
            return Result.error("参数为空");
        }
        BizNceEntity entity = bizNceService.getById(id);
        if (entity == null) {
            return Result.error("NCE为空");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (bizNceService.removeById(id)) {
                UpdateWrapper<BizPresetEntity> updateWrapper = new UpdateWrapper<>();
                //可将指定字段更新为null
                updateWrapper.set("nce_id", null);
                updateWrapper.set("begin", 0);
                updateWrapper.set("end", 0);
                updateWrapper.eq("nce_id", id);
                bizPresetService.update(updateWrapper);
                logUtil.addLog("NCE管理","NCE[" + entity.getName() + "]删除成功！", LogConstant.OPERATE_TYPE_4, loginUser);
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
