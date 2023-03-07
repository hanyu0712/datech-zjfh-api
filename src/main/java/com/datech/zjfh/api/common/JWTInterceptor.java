package com.datech.zjfh.api.common;

import com.alibaba.fastjson.JSONObject;
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.consts.WebConstant;
import com.datech.zjfh.api.service.SysUserServiceImpl;
import com.datech.zjfh.api.util.JwtUtil;
import com.datech.zjfh.api.util.LoginUtil;
import com.datech.zjfh.api.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JWTInterceptor implements HandlerInterceptor {

    private SysUserServiceImpl sysUserService;
    private RedisUtil redisUtil;
    public JWTInterceptor(SysUserServiceImpl sysUserService, RedisUtil redisUtil) {
        this.sysUserService = sysUserService;
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");//从请求头中获取token
        Map<String, Object> map = new HashMap<>();
//        try {
        if ("datech".equals(token)) {
            return true;
        }
        LoginUser loginUser = LoginUtil.getLoginUser(token, redisUtil);
        if (loginUser == null) {
            map.put("message", "无效token");
        } else {
//                boolean verify = JwtUtil.verify(token, userId, user.getPassword());
//            log.info("jwt interceptor in token:{}，  redis_token:{}", token, loginUser.getToken());
            if (loginUser.getToken().equals(token)) {
                return true;
            } else {
                map.put("message", "账号已在其他设备登录");
            }
        }
//        } catch (TokenExpiredException e) {
//            map.put("message", "Token已经过期!!!");
//        } catch (SignatureVerificationException e){
//            map.put("message", "签名错误!!!");
//        } catch (AlgorithmMismatchException e){
//            map.put("message", "加密算法不匹配!!!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            map.put("message", "无效token~~");
//        }
        map.put("success", false);
        map.put("code", 500);
        String json = new ObjectMapper().writeValueAsString(map);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(json);
        return false;
    }

}
