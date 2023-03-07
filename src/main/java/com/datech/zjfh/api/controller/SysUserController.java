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
import com.datech.zjfh.api.entity.SysRoleEntity;
import com.datech.zjfh.api.entity.SysUserEntity;
import com.datech.zjfh.api.service.SysOrgServiceImpl;
import com.datech.zjfh.api.service.SysRoleServiceImpl;
import com.datech.zjfh.api.service.SysUserServiceImpl;
import com.datech.zjfh.api.util.*;
import com.datech.zjfh.api.vo.SysUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提供用户数据接口
 */
@RestController
@RequestMapping("/sys")
@Slf4j
public class SysUserController {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private SysUserServiceImpl sysUserService;
    @Resource
    private SysRoleServiceImpl sysRoleService;
    @Resource
    private SysOrgServiceImpl sysOrgService;


    /**
     * 查询
     */
    @GetMapping(value = "/user/{id}")
    public Result<Object> getUser(@PathVariable("id") Integer id) {
        SysUserEntity user = sysUserService.getById(id);
        SysUserVo vo = BeanCopierUtil.copyBean(user, SysUserVo.class);
        SysRoleEntity role = sysRoleService.getById(vo.getRoleId());
        if (role != null) {
            vo.setRoleName(role.getName());
        }
        return Result.OK(vo);
    }

    /**
     * 新增用户
     */
    @PostMapping(value = "/user")
    public Result<Object> saveUser(@RequestBody SysUserEntity vo) {
        log.info("------------  user save in ：{}---------", vo);
        if (vo == null || StringUtils.isBlank(vo.getUsername()) || StringUtils.isBlank(vo.getRealname()) || StringUtils.isBlank(vo.getPhone())
                || StringUtils.isBlank(vo.getPassword()) || vo.getRoleId() == null || vo.getOrgId() == null || vo.getOrgId() == 0) {
            return Result.error("参数为空");
        }
        try {
            if (vo.getOrgId() < 1 || sysOrgService.getById(vo.getOrgId()) == null) {
                return Result.error("组织节点为空");
            }
            LambdaQueryWrapper<SysUserEntity> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(SysUserEntity::getUsername, vo.getUsername());
            queryWrapper.eq(SysUserEntity::getOrgId, vo.getOrgId());
            queryWrapper.eq(SysUserEntity::getDelFlag, 0);
            List<SysUserEntity> entityOriList = sysUserService.list(queryWrapper);
            if (entityOriList != null && entityOriList.size() > 0) {
                return Result.error("用户名称重复");
            }
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            SysUserEntity entity = BeanCopierUtil.copyBean(vo, SysUserEntity.class);
            if (sysUserService.saveEntity(entity, loginUser)) {
                logUtil.addLog("用户管理","用户[" + vo.getUsername() + "]创建成功！", LogConstant.OPERATE_TYPE_2, loginUser);
                return Result.OK(true);
            } else {
                return Result.error("操作失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error("操作异常：" + e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }
    }

    /**
     * 修改
     */
    @PutMapping(value = "/user")
    public Result<Object> editUser(@RequestBody SysUserEntity vo) {
        log.info("------------  user edit in ...---------");
        if (vo == null || vo.getId() == null || vo.getRoleId() == null || StringUtils.isBlank(vo.getUsername())
                || StringUtils.isBlank(vo.getRealname()) || StringUtils.isBlank(vo.getPhone())) {
            return Result.error("参数为空");
        }
        if (vo.getRoleId() == 1) {
            return Result.error("不可修改为系统预留角色");
        }
        SysUserEntity entityOri = sysUserService.getById(vo.getId());
        if (entityOri == null) {
            return Result.error(" 用户为空");
        }
        LambdaQueryWrapper<SysUserEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.ne(SysUserEntity::getId, vo.getId());
        queryWrapper.eq(SysUserEntity::getUsername, vo.getUsername());
        queryWrapper.eq(SysUserEntity::getOrgId, entityOri.getOrgId());
        List<SysUserEntity> entityOriList = sysUserService.list(queryWrapper);
        if (entityOriList != null && entityOriList.size() > 0) {
            return Result.error("用户名称重复");
        }
        SysUserEntity entityNew = BeanCopierUtil.copyBean(entityOri, SysUserEntity.class);
        entityNew.setUsername(vo.getUsername());
        entityNew.setRealname(vo.getRealname());
        entityNew.setRoleId(vo.getRoleId());
        entityNew.setPhone(vo.getPhone());
        try {
            //当前登录账户
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (loginUser != null) {
                entityNew.setUpdateBy(loginUser.getUsername());
            }
            if (sysUserService.updateById(entityNew)) {
                logUtil.addLog("用户管理","用户['" + entityNew.getUsername() + "-" + entityNew.getRealname() + "-'" + entityNew.getRoleId() + "-'" + entityNew.getOrgId() + "']修改成功！", LogConstant.OPERATE_TYPE_3, loginUser);
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
    @PutMapping(value = "/user/password")
    public Result<Object> updatePassword(@RequestBody SysUserEntity vo) {
        log.info("------------  user edit in ...---------");
        if (vo == null || vo.getId() == null || StringUtils.isBlank(vo.getPassword())) {
            return Result.error("参数为空");
        }
        SysUserEntity entityOri = sysUserService.getById(vo.getId());
        if (entityOri == null) {
            return Result.error(" 用户为空");
        }
        SysUserEntity entityNew = BeanCopierUtil.copyBean(entityOri, SysUserEntity.class);
        entityNew.setPassword(vo.getPassword());
        try {
            //当前登录账户
            LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
            if (sysUserService.updatePassword(entityNew, loginUser)) {
                redisUtil.del(WebConstant.USER_LOGIN_TOKEN + entityNew.getId());
                logUtil.addLog("用户管理","用户修改密码成功！", LogConstant.OPERATE_TYPE_3, loginUser);
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
     */
    @DeleteMapping(value = "/user/{id}")
    public Result<Object> removeUser(@PathVariable("id") Integer id) {
        log.info("------------  user remove in ...---------");
        if (id == null) {
            return Result.error("参数为空");
        }
        SysUserEntity entity = sysUserService.getById(id);
        if (entity == null) {
            return Result.error("用户为空");
        }
        if (entity.getId() == 1) {
            return Result.error("系统管理员无法删除");
        }
        //当前登录账户
        LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
        try {
            entity.setDelFlag(1);
            if (loginUser != null) {
                entity.setUpdateBy(loginUser.getUsername());
            }
            if (sysUserService.updateById(entity)) {
                logUtil.addLog("用户管理","用户[" + entity.getUsername() + "]删除成功！", LogConstant.OPERATE_TYPE_4, loginUser);
                redisUtil.del(WebConstant.USER_LOGIN_TOKEN + id);
                return Result.OK(true);
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("操作异常：" + e.getMessage());
        }

    }

    @GetMapping(value = "/user/pageList")
    public Result<Object> pageList(Integer orgId, @RequestParam(defaultValue = "1") Integer pageNo,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        if (orgId == null) {
            return Result.error("参数为空");
        }
        //查询角色 ID
        /*List<Integer> roleIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(param)) {
            List<SysRoleEntity> roleList = sysRoleService.list(new QueryWrapper<>(new SysRoleEntity()).like("name", param));
            roleIdList = roleList.stream().map(SysRoleEntity::getId).collect(Collectors.toList());
        }*/
        LambdaQueryWrapper<SysUserEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysUserEntity::getDelFlag, 0);
        queryWrapper.eq(SysUserEntity::getOrgId, orgId);
        /*if (StringUtils.isNotBlank(param)) {
            List<Integer> finalRoleIdList = roleIdList;
            queryWrapper.and(a -> a.like(SysUserEntity::getUsername, param)
                    .or().like(SysUserEntity::getRealname, param)
                    .or().like(SysUserEntity::getPhone, param)
                    .or(finalRoleIdList.size() > 0, b -> b.in(SysUserEntity::getRoleId, finalRoleIdList)));
        }*/
        queryWrapper.orderByAsc(SysUserEntity::getCreateTime);
        Page<SysUserEntity> entityPage = sysUserService.page(new Page<>(pageNo, pageSize), queryWrapper);
        Page<SysUserVo> voPage = convertVoPage(entityPage);
        return Result.OK(voPage);
    }

    private Page<SysUserVo> convertVoPage(Page<SysUserEntity> entityPage) {
        Page<SysUserVo> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<SysUserVo> voList = new ArrayList<>();
        if (entityPage.getRecords().size() > 0) {
            //设置角色名称
            List<SysRoleEntity> roleList = sysRoleService.list();
            Map<Integer, String> roleMap = roleList.stream().collect(Collectors.toMap(SysRoleEntity::getId,
                    SysRoleEntity::getName));
            entityPage.getRecords().forEach(o -> voList.add(BeanCopierUtil.copyBean(o, SysUserVo.class)));
            voList.forEach(o -> o.setRoleName(roleMap.get(o.getRoleId())));
        }
        voPage.setRecords(voList);
        return voPage;
    }


}
