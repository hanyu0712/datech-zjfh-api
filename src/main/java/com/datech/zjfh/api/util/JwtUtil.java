package com.datech.zjfh.api.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.datech.zjfh.api.common.exception.FastBootException;

import java.util.Date;

public class JwtUtil {

    /**
     * 校验token是否正确
     *
     * @param token  密钥
     * @param secret 用户的密码
     * @return 是否正确
     */
    public static boolean verify(String token, Integer userId, String secret) {
        // 根据密码生成JWT效验器
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).withClaim("userId", userId).build();
        // 效验TOKEN
        DecodedJWT jwt = verifier.verify(token);
        return true;
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     *
     * @return token中包含的用户名
     */
    public static Integer getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asInt();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 生成签名,5min后过期
     *
     * @param userId 用户ID
     * @param secret   用户的密码
     * @return 加密的token
     */
    public static String sign(Integer userId, String secret) {
        Date date = new Date(System.currentTimeMillis() + 1 * 60 * 1000);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        // 附带username信息
        return JWT.create().withClaim("userId", userId)
                .withExpiresAt(date)    //添加过期时间是为了每次生成token都不同，系统登录不做过期校验
                .sign(algorithm);
    }

    /**
     * 根据request中的token获取用户账号
     *
     * @param request
     * @return
     * @throws FastBootException
     */
//    public static String getUserNameByToken(HttpServletRequest request) throws FastBootException {
//        String accessToken = request.getHeader(WebConstant.X_ACCESS_TOKEN);
//        String username = getUserId(accessToken);
//        if (oConvertUtil.isEmpty(username)) {
//            throw new FastBootException("未获取到用户");
//        }
//        return username;
//    }

}
