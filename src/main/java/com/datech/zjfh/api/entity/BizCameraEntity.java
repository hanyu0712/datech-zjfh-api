package com.datech.zjfh.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("biz_camera")
public class BizCameraEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     *  线路 ID
     */
//    @TableField(value = "line_id")
//    private Integer lineId;
    /**
     *  ivs ID
     */
    @TableField(value = "ivs_id")
    private Integer ivsId;
    /**
     *  组织节点 ID
     */
    @TableField(value = "org_id")
    private Integer orgId;
    /**
     *  组织节点 ID
     */
    @TableField(value = "area")
    private String area;

    /**
     *  名称
     */
    @TableField(value = "name")
    private String name;

    /**
     *  编码
     */
    @TableField(value = "code")
    private String code;
    /**
     *  域编码
     */
    @TableField(value = "domain_code")
    private String domainCode;
    /**
     *  域编码
     */
    @TableField(value = "nvr_code")
    private String nvrCode;

    /**
     *  告警订阅ID
     */
    @TableField(value = "subscribe_id")
    private String subscribeId;

    /**
     *  IP 地址
     */
    @TableField(value = "device_ip")
    private String  deviceIp;

    /**
     *  类型
     */
    @TableField(value = "vendor_type")
    private String vendorType;
    /**
     *  经度
     */
    @TableField(value = "longitude")
    private String longitude;
    /**
     *  纬度
     */
    @TableField(value = "latitude")
    private String latitude;

    /**
     *  在线状态，0：离线，1：在线， 2：休眠
     */
    @TableField(value = "status")
    private Integer status;
    /**
     *  订阅告警开关，0：关闭，1；开启
     */
    @TableField(value = "subs_enable")
    private Integer subsEnable;
    /**
     *  告警类型，1：雷摄，2；光摄
     */
    @TableField(value = "alarm_type")
    private Integer alarmType;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
