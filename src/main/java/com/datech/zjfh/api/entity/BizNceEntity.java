package com.datech.zjfh.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("biz_nce")
public class BizNceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * 线路id
     */
    @TableField(value = "line_id")
    private Integer lineId;

    @TableField(value = "name")
    private String name;

    @TableField(value = "length")
    private Float length;

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

    @TableField(value = "session")
    private String session;

    @TableField(value = "roarand")
    private String roarand;
    /**
     *  订阅告警标识
     */
    @TableField(value = "identifier")
    private String identifier;
    /**
     *  在线状态，0：不在线，1：在线
     */
    @TableField(value = "state")
    private Integer state;
    /**
     *  订阅，0：关闭，1：启用
     */
    @TableField(value = "subs_enable")
    private Integer subsEnable;


}
