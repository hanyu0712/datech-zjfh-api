package com.datech.zjfh.api.util;

import com.alibaba.fastjson.JSONObject;
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.consts.WebConstant;

public class LoginUtil {

    public static LoginUser getLoginUser(String token, RedisUtil redisUtil) {
        Integer loginUserId = JwtUtil.getUserId(token);
        Object obj = redisUtil.get(WebConstant.USER_LOGIN_TOKEN + loginUserId);
        if (obj != null) {
            LoginUser loginUser = JSONObject.parseObject(obj.toString(), LoginUser.class);
            return loginUser;
        }
        return null;
    }

}
