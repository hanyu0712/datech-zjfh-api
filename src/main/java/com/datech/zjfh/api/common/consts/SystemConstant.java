package com.datech.zjfh.api.common.consts;

/**
 * 系统模块常量定义
 */
public interface SystemConstant {

    /**
     * 租户系统组管理员角色
     */
    String TENANT_SYS_GROUP_ADMIN_ROLE = "sysgroupadmin";

    /**
     * 基础编码(获取随机码使用)
     */
    String BASE_CODE = "qwertyuiplkjhgfdsazxcvbnmQWERTYUPLKJHGFDSAZXCVBNM1234567890";

    String PROJECT_NAME = "DATECH";

    /**
     * 平台租户ID
     */
    String SYS_TENANT_ID = "0";

    /**
     * 冻结
     */
    Integer FREEZE = 0;
    /**
     * 正常
     */
    Integer UNFREEZE = 1;

    /**
     *  0：一级菜单
     */
    Integer MENU_TYPE_0  = 0;
    /**
     *  1：子菜单
     */
    Integer MENU_TYPE_1  = 1;
    /**
     *  2：按钮权限
     */
    Integer MENU_TYPE_2  = 2;

}
