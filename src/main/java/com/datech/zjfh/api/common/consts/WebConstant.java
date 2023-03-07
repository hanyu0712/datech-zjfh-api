package com.datech.zjfh.api.common.consts;

public interface WebConstant {
    /** 微服务读取配置文件属性 服务地址 */
    String CLOUD_SERVER_KEY = "spring.cloud.nacos.discovery.server-addr";

    /** 请求Header中的token */
    String X_ACCESS_TOKEN = "X-Access-Token";

    /** 请求Header中的当前应用编码 */
    String X_APP_CODE = "X-App-Code";

    /** 请求Header中的租户ID */
    String X_TENANT_ID = "X-Tenant-Id";

    /** 登录用户Shiro权限缓存KEY前缀 */
    String PREFIX_USER_SHIRO_CACHE  = "shiro:cache:cn.com.bjev.v2x.common.web.shiro.ShiroRealm.authorizationCache:";

    /** 登录用户Token令牌缓存KEY前缀 */
    String USER_LOGIN_TOKEN = "user_login_token_";

    /** 访问认证未通过(510) */
    Integer SC_NO_AUTHC = 510;

    /** 无权限访问(511) */
    Integer SC_NO_AUTHZ = 511;

}
