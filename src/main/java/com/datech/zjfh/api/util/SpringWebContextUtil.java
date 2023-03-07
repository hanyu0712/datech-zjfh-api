package com.datech.zjfh.api.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
public class SpringWebContextUtil {

    /**
     * 获取HttpServletRequest
     */
    public static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     *  获取项目根路径 basePath
     */
    public static String getDomain(){
        HttpServletRequest request = getHttpServletRequest();
        StringBuffer url = request.getRequestURL();
        //微服务情况下，获取gateway的basePath
        String basePath = request.getHeader("X_GATEWAY_BASE_PATH");
        if(StringUtils.isNotBlank(basePath)){
            return basePath;
        }else{
            return url.delete(url.length() - request.getRequestURI().length(), url.length()).toString();
        }
    }

    public static String getOrigin(){
        HttpServletRequest request = getHttpServletRequest();
        return request.getHeader("Origin");
    }

}
