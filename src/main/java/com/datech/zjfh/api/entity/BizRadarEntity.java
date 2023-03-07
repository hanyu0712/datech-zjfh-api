package com.datech.zjfh.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("biz_radar")
public class BizRadarEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "type")
    private Integer type;

    @TableField(value = "camera_ip")
    private String cameraIp;

    @TableField(value = "ip")
    private String ip;

    /**
     *  账号
     */
    @TableField(value = "account")
    private String account;
    /**
     *  密码
     */
    @TableField(value = "password")
    private String password;



}
