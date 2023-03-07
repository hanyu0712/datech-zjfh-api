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
import com.datech.zjfh.api.common.consts.WebConstant;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.entity.BizPresetEntity;
import com.datech.zjfh.api.entity.SysOrgEntity;
import com.datech.zjfh.api.entity.SysUserEntity;
import com.datech.zjfh.api.service.BizCameraServiceImpl;
import com.datech.zjfh.api.service.SysOrgServiceImpl;
import com.datech.zjfh.api.service.SysUserServiceImpl;
import com.datech.zjfh.api.util.*;
import com.datech.zjfh.api.vo.SysOrgTreeVo;
import com.datech.zjfh.api.vo.SysOrgVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 提供用户数据接口
 */
@RestController
@RequestMapping("/sys")
@Slf4j
public class SysOrgController {
    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private SysOrgServiceImpl sysOrgService;
    @Resource
    private SysUserServiceImpl sysUserService;

    @GetMapping("/org/tree")
    public Result<Object> tree() {
        log.info("------------  org tree in ...---------");
        Result<SysOrgTreeVo> result = new Result<>();
        try {
            SysOrgTreeVo root = sysOrgService.tree();
            result.setSuccess(true);
            return Result.OK(root);
        } catch (Exception e) {
            log.error("查询组织节点数据失败，信息：{}", e.getMessage());
            return Result.error("查询组织节点数据失败");
        }
    }
    @GetMapping("/org/child/pageList")
    public Result<Object> orgChildPageList(Integer id, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("------------  org child pageList in :{}---------", id);
        if (id == null) {
            return Result.error("参数为空");
        }
        try {
            SysOrgEntity root = sysOrgService.getById(id);
            if (root == null) {
                return Result.error("组织节点为空");
            }
            LambdaQueryWrapper<SysOrgEntity> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(SysOrgEntity::getLevel, root.getLevel() + 1);
            queryWrapper.eq(SysOrgEntity::getPid, id);
            Page<SysOrgEntity> entityPage = sysOrgService.page(new Page<>(pageNo, pageSize), queryWrapper);
            Page<SysOrgVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
            List<SysOrgVo> voList = new ArrayList<>();
            if (entityPage.getRecords().size() > 0) {
                for (SysOrgEntity entity : entityPage.getRecords()) {
                    SysOrgVo vo = BeanCopierUtil.copyBean(entity, SysOrgVo.class);
                    if (vo.getLevel() == 1) {
                        vo.setType("总局");
                    }
                    if (vo.getLevel() == 2) {
                        vo.setType("站段");
                    }
                    if (vo.getLevel() == 3) {
                        vo.setType("车间");
                    }
                    if (vo.getLevel() == 4) {
                        vo.setType("工区");
                    }
                    vo.setPName(root.getName());
                    voList.add(vo);
                }
            }
            voPage.setRecords(voList);
            return Result.OK(voPage);
        } catch (Exception e) {
            log.error("查询组织节点数据失败，信息：{}", e.getMessage());
            return Result.error("查询组织节点子列表数据失败");
        }
    }
    @GetMapping("/org/level4")
    public Result<Object> orgLevel() {
        log.info("------------  org level4 in ...---------");
        try {
            LambdaQueryWrapper<SysOrgEntity> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(SysOrgEntity::getLevel, 4);
            List<SysOrgEntity> entityList = sysOrgService.list(queryWrapper);
            Map<String, Integer> map = entityList.stream().collect(Collectors.toMap(SysOrgEntity::getName, SysOrgEntity::getId));
            return Result.OK(map);
        } catch (Exception e) {
            log.error("查询组织节点数据失败，信息：{}", e.getMessage());
            return Result.error("查询组织节点数据失败");
        }
    }


    /**
     * 新增组织节点
     */
    @PostMapping(value = "/org")
    public Result<Object> saveOrg(@RequestBody SysOrgVo orgVo) {
        log.info("------------  org save in :{}---------", orgVo);
        if (orgVo == null || orgVo.getPid() == null || StringUtils.isBlank(orgVo.getName()) || StringUtils.isBlank(orgVo.getResponsible())) {
            return Result.error("参数为空");
        }
        SysOrgEntity parent = sysOrgService.getById(orgVo.getPid());
        if (parent == null) {
            return Result.error("父节点为空");
        }
        if (parent.getLevel() == null || parent.getLevel() >= 4) {
            return Result.error("组织节点层级超出限制");
        }
        LambdaQueryWrapper<SysOrgEntity> queryWrapper = Wrappers.lambdaQuery();
//        queryWrapper.eq(SysOrgEntity::getPid, orgVo.getPid());
        queryWrapper.eq(SysOrgEntity::getName, orgVo.getName());
        List<SysOrgEntity> entityOri = sysOrgService.list(queryWrapper);
        if (entityOri != null && entityOri.size() > 0) {
            return Result.error("组织节点名称重复");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (sysOrgService.save(orgVo)) {
                logUtil.addLog("区域管理","组织节点[" + orgVo.getName() + "]创建成功！", LogConstant.OPERATE_TYPE_2, loginUser);
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
    @PutMapping(value = "/org")
    public Result<Object> editOrg(@RequestBody SysOrgVo vo) {
        log.info("------------  org edit in :{}---------", vo);
        if (vo == null || vo.getId() == null || StringUtils.isBlank(vo.getName()) || StringUtils.isBlank(vo.getResponsible())
                || StringUtils.isBlank(vo.getDescription())) {
            return Result.error("参数为空");
        }
        SysOrgEntity entityOri = sysOrgService.getById(vo.getId());
        if (entityOri == null) {
            return Result.error(" 组织节点为空");
        }
        LambdaQueryWrapper<SysOrgEntity> queryWrapper = Wrappers.lambdaQuery();
//        queryWrapper.eq(SysOrgEntity::getPid, vo.getPid());
        queryWrapper.eq(SysOrgEntity::getName, vo.getName());
        List<SysOrgEntity> entityOriList = sysOrgService.list(queryWrapper);
        if (entityOriList != null && entityOriList.size() > 1) {
            return Result.error("组织节点名称重复");
        }
        SysOrgEntity entityNew = BeanCopierUtil.copyBean(entityOri, SysOrgEntity.class);
        entityNew.setName(vo.getName());
        entityNew.setResponsible(vo.getResponsible());
        entityNew.setDescription(vo.getDescription());
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (sysOrgService.updateById(entityNew)) {
                logUtil.addLog("区域管理","组织节点['" + entityOri.getName() + "-" + entityOri.getResponsible() + "-'" + entityOri.getDescription() + "'修改为'" + entityNew.getName() + "-" + entityNew.getResponsible() + "-'" + entityNew.getDescription() + "']修改成功！", LogConstant.OPERATE_TYPE_3, loginUser);
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
    @DeleteMapping(value = "/org/{id}")
    public Result<Object> removeOrg(@PathVariable("id") Integer id) {
        log.info("------------  org remove in :{}---------", id);
        if (id == null) {
            return Result.error("参数为空");
        }
        SysOrgEntity entity = sysOrgService.getById(id);
        if (entity == null) {
            return Result.error("组织节点为空");
        }
        ArrayList<Integer> idList = new ArrayList<>(Collections.singletonList(id));
        sysOrgService.getChildIdList(id, idList);
        LambdaQueryWrapper<SysUserEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(SysUserEntity::getOrgId, idList);
        queryWrapper.eq(SysUserEntity::getDelFlag, 0);
        if (sysUserService.count(queryWrapper) > 0) {
            return Result.error("操作失败，请先删除组织节点的用户");
        }
        try {
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (sysOrgService.deepDelete(id)) {

                logUtil.addLog("区域管理","组织节点[" + entity.getName() + "]删除成功！", LogConstant.OPERATE_TYPE_4, loginUser);
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
