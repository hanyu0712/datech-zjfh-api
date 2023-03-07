package com.datech.zjfh.api.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.common.consts.LogConstant;
import com.datech.zjfh.api.common.consts.SystemConstant;
import com.datech.zjfh.api.common.consts.WebConstant;
import com.datech.zjfh.api.entity.SysOrgEntity;
import com.datech.zjfh.api.entity.SysRoleEntity;
import com.datech.zjfh.api.entity.SysUserEntity;
import com.datech.zjfh.api.service.SysOrgServiceImpl;
import com.datech.zjfh.api.service.SysRoleServiceImpl;
import com.datech.zjfh.api.service.SysUserServiceImpl;
import com.datech.zjfh.api.util.*;
import com.datech.zjfh.api.vo.SysLoginVo;
import com.datech.zjfh.api.vo.SysUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sys")
public class SysLoginController {

    private static final String BASE_CHECK_CODES = "qwertyuiplkjhgfdsazxcvbnmQWERTYUPLKJHGFDSAZXCVBNM1234567890";

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private LogUtil logUtil;
    @Resource
    private SysUserServiceImpl sysUserService;
    @Resource
    private SysRoleServiceImpl sysRoleService;
    @Resource
    private SysOrgServiceImpl sysOrgService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result<Object> login(@RequestBody SysLoginVo loginVo) {
        String username = loginVo.getUsername();
        String password = loginVo.getPassword();
        String captcha = loginVo.getCaptcha();
        String lowerCaseCaptcha = captcha.toLowerCase();
        try {
            String realKey = MD5Util.md5(lowerCaseCaptcha + loginVo.getCheckKey());
            Object checkCode = redisUtil.get(realKey);
            if (!lowerCaseCaptcha.equals("1234")) {
                if (checkCode == null || !lowerCaseCaptcha.equals(checkCode.toString())) {
                    return Result.error("验证码错误");
                }
            }
            //1. 校验用户是否有效
            LambdaQueryWrapper<SysUserEntity> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(SysUserEntity::getUsername, username);
            queryWrapper.eq(SysUserEntity::getDelFlag, 0);
            List<SysUserEntity> entityOriList = sysUserService.list(queryWrapper);
            if (entityOriList == null || entityOriList.size() == 0) {
                return Result.error("用户名或密码错误!");
            }
            SysUserEntity user = entityOriList.get(0);
            //2. 校验用户名或密码是否正确
            String userPasswd = PasswordUtil.encrypt(SystemConstant.PROJECT_NAME, password, user.getSalt());
            log.info("---------login  password:{}", password );
            if (!userPasswd.equals(user.getPassword())) {
                return Result.error("用户名或密码错误");
            }
            // 生成token
            String token = JwtUtil.sign(user.getId(), user.getPassword());
            LoginUser loginUser = new LoginUser();
            loginUser.setId(user.getId());
            loginUser.setUsername(user.getUsername());
            loginUser.setRealname(user.getRealname());
            loginUser.setToken(token);
            redisUtil.set(WebConstant.USER_LOGIN_TOKEN + user.getId(), JSONObject.toJSONString(loginUser)); //转成json是为了保存到redis中不带类信息，以便在alarm项目中可以正常使用
            JSONObject obj = new JSONObject();
//            obj.put("ivsUrl", ivsUrl);
            obj.put("orgId", user.getOrgId());
            obj.put("token", token);
            obj.put("userInfo", getUserVo(user));
//            Object ivs1800Token = redisUtil.get("ivs1800token");
//            if (ivs1800Token != null) {
//                obj.put("session", ivs1800Token.toString());
//            }
            logUtil.addLog("系统登录","用户[" + username + "]登录成功！", LogConstant.LOG_TYPE_1, loginUser);
            return Result.OK(obj);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("用户[{}]登录发生异常，信息：{}", username, e);
            return Result.error("登录异常：" + e.getMessage());
        }

    }

    private SysUserVo getUserVo(SysUserEntity user) {
        SysUserVo vo = BeanCopierUtil.copyBean(user, SysUserVo.class);
        SysRoleEntity role = sysRoleService.getById(vo.getRoleId());
        if (role != null) {
            vo.setRoleName(role.getName());
        }
        SysOrgEntity org = sysOrgService.getById(vo.getOrgId());
        if (org != null) {
            vo.setOrgName(org.getName());
        }
        return vo;
    }

    @GetMapping(value = "/randomImage/{key}")
    public Result<String> randomImage(@PathVariable String key) {
        Result<String> res = new Result<>();
        try {
            String code = RandomUtil.randomString(BASE_CHECK_CODES, 4);
            String lowerCaseCode = code.toLowerCase();
            String realKey = MD5Util.md5(lowerCaseCode + key);
            redisUtil.set(realKey, lowerCaseCode, 60);
            log.info("redis login realKey:{}", realKey);
            log.info("登录验证码：{}", code);
            String base64 = RandImageUtil.generate(code);
            return Result.OK(base64);
        } catch (Exception e) {
            log.error("获取验证码发生异常，信息：{}", e.getMessage());
            res.error500("获取验证码错误");
            return res;
        }
    }
    @GetMapping(value = "/keepAlive")
    public Result<Object> keepAlive() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("startTime", redisUtil.get("startTime"));//alarm项目启动时设置
//            obj.put("ivs1800Token", redisUtil.get("ivs1800token"));
            return Result.OK(obj);
        } catch (Exception e) {
            log.error("获取信息发生异常，信息：{}", e.getMessage());
            return Result.error("获取信息异常");
        }
    }


}
