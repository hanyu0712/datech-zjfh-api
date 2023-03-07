package com.datech.zjfh.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysUserVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String username;
    private String realname;
    private String phone;
//    private String password;
    private Integer roleId;
    private String roleName;
    private Integer orgId;
    private String orgName;
    private Date createTime;
    private Date updateTime;
}
