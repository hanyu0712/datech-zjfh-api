package com.datech.zjfh.api.config;

import com.datech.zjfh.api.common.JWTInterceptor;
import com.datech.zjfh.api.service.SysUserServiceImpl;
import com.datech.zjfh.api.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Resource
    private SysUserServiceImpl sysUserService;
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JWTInterceptor(sysUserService, redisUtil)).
//                excludePathPatterns("/sys/**")  // 放行的请求路径
                excludePathPatterns("/sys/login", "/sys/randomImage/**")  // 放行的请求路径
                .addPathPatterns("/sys/**", "/biz/**"); // 拦截所有请求路径除了上面放行的
    }
}
