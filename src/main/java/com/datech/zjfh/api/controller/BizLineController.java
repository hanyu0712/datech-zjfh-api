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
import com.datech.zjfh.api.entity.*;
import com.datech.zjfh.api.service.BizCameraServiceImpl;
import com.datech.zjfh.api.service.BizIvsServiceImpl;
import com.datech.zjfh.api.service.BizLineServiceImpl;
import com.datech.zjfh.api.util.*;
import com.datech.zjfh.api.vo.BizLineVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/biz")
@Slf4j
public class BizLineController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private BizLineServiceImpl bizLineService;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizIvsServiceImpl bizIvsService;


    @GetMapping(value = "/line/pageList")
    public Result<Object> pageList(String name, @RequestParam(defaultValue = "1") Integer pageNo,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<BizLineEntity> queryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.likeLeft(BizLineEntity::getName, name);
        }
        queryWrapper.orderByAsc(BizLineEntity::getNo);
        Page<BizLineEntity> entityPage = bizLineService.page(new Page<>(pageNo, pageSize), queryWrapper);
        Page<BizLineVo> voPage = convertVoPage(entityPage);
        return Result.OK(voPage);
    }

    private Page<BizLineVo> convertVoPage(Page<BizLineEntity> entityPage) {
        Page<BizLineVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<BizLineVo> voList = new ArrayList<>();
        if (entityPage.getRecords().size() > 0) {
            entityPage.getRecords().forEach(o -> {
                BizLineVo vo = BeanCopierUtil.copyBean(o, BizLineVo.class);
                List<Integer> ivsIdList = bizIvsService.getIvsList(vo.getId());
                if (CollectionUtils.isNotEmpty(ivsIdList)) {
                    LambdaQueryWrapper<BizCameraEntity> queryWrapper = Wrappers.lambdaQuery();
                    queryWrapper.in(BizCameraEntity::getIvsId, ivsIdList);
                    vo.setCameraCount(bizCameraService.count(queryWrapper));
                } else {
                    vo.setCameraCount(0);
                }
                voList.add(vo);
            });
        }
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 新增
     */
    @PostMapping(value = "/line")
    public Result<Object> saveLine(@RequestBody BizLineEntity entity) {
        log.info("------------  line save in :{}---------", entity);
        if (entity == null || StringUtils.isBlank(entity.getNo()) || StringUtils.isBlank(entity.getName()) || entity.getLength() == null) {
            return Result.error("参数为空");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (bizLineService.save(entity)) {
                logUtil.addLog("线路管理","线路[" + entity.getName() + "]创建成功！", LogConstant.OPERATE_TYPE_2, loginUser);
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
    @PutMapping(value = "/line")
    public Result<Object> editLine(@RequestBody BizLineEntity entity) {
        log.info("------------  line edit in :{}---------", entity);
        if (entity == null || entity.getId() == null || entity.getLength() == null || StringUtils.isBlank(entity.getName())
                || StringUtils.isBlank(entity.getNo())) {
            return Result.error("参数为空");
        }
        BizLineEntity entityOri = bizLineService.getById(entity.getId());
        if (entityOri == null) {
            return Result.error(" 线路为空");
        }
        try {
            if (bizLineService.updateById(entity)) {
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
    @DeleteMapping(value = "/line/{id}")
    public Result<Object> removeLine(@PathVariable("id") Integer id) {
        log.info("------------  line remove in :{}---------", id);
        if (id == null) {
            return Result.error("参数为空");
        }
        BizLineEntity entity = bizLineService.getById(id);
        if (entity == null) {
            return Result.error("线路为空");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (bizLineService.removeById(id)) {
                UpdateWrapper<BizCameraEntity> updateWrapper = new UpdateWrapper<>();
                //可将指定字段更新为null
                updateWrapper.set("line_id", null);
                updateWrapper.eq("line_id", id);
                bizCameraService.update(updateWrapper);
                logUtil.addLog("线路管理","线路[" + entity.getName() + "]删除成功！", LogConstant.OPERATE_TYPE_4, loginUser);
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
     * 查询全部线路
     */
    @GetMapping(value = "/lines")
    public Result<Object> getAllLines() {
        List<BizLineEntity> entityList = bizLineService.list();
        Map<Integer, String> entityMap = entityList.stream().collect(Collectors.toMap(BizLineEntity::getId, BizLineEntity::getName));
        return Result.OK(entityMap);
    }

}
