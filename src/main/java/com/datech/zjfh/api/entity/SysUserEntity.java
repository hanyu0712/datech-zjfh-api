package com.datech.zjfh.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_user")
public class SysUserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "username")
    private String username;

    @TableField(value = "realname")
    private String realname;

    @TableField(value = "phone")
    private String phone;

    @TableField(value = "password")
    private String password;

    @TableField(value = "salt")
    private String salt;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "create_by")
    private String createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableField(value = "update_by")
    private String updateBy;

    @TableField(value = "del_flag")
    private int delFlag;

    @TableField(value = "role_id")
    private Integer roleId;

    @TableField(value = "org_id")
    private Integer orgId;




}
