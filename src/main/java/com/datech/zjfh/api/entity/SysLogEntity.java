package com.datech.zjfh.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_log")
public class SysLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 日志类型(1-登录日志，2-操作日志)
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 日志内容
     */
    @TableField(value = "content")
    private String content;


    /**
     * 操作用户名
     */
    @TableField(value = "opUsername")
    private String opUsername;

    /**
     * 操作人姓名
     */
    @TableField(value = "opRealname")
    private String opRealname;

    /**
     *  IP 地址
     */
    @TableField(value = "device_ip")
    private String  deviceIp;
    /**
     *  IP 地址
     */
    @TableField(value = "menu")
    private String  menu;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
