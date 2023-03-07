package com.datech.zjfh.api.vo;

import lombok.Data;

import java.util.Date;

/**
 * 登录信息对象
 */
@Data
public class SysOrgVo {
    private Integer id;
    private String name;
    private Integer level;
    private String responsible;
    private String description;
    private String type;
    private String pName;
    private Integer pid;
    private Date createTime;


}
