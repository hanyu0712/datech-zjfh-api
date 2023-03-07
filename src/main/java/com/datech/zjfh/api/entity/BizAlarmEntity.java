package com.datech.zjfh.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("biz_alarm")
public class BizAlarmEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 线路ID
     */
    @TableField(value = "line_id")
    private Integer lineId;
    /**
     * 摄像头编码
     */
    @TableField(value = "camera_code")
    private String cameraCode;

    /**
     *  告警ID
     */
    @TableField(value = "notification_id")
    private String notificationId;
    /**
     *  告警图片ID
     */
    @TableField(value = "image_id")
    private String imageId;

    /**
     *  告警等级
     */
    @TableField(value = "level")
    private int level;

    /**
     *  状态
     */
    @TableField(value = "state")
    private Integer state;

    /**
     *  告警时间
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @TableField(value = "trigger_time")
    private Date triggerTime;

    /**
     * 创建时间
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 解除时间
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @TableField(value = "clear_time")
    private Date clearTime;

    /**
     *  解除人
     */
    @TableField(value = "clear_user")
    private String clearUser;

    /**
     *  误报，0：否，1：是
     */
    @TableField(value = "false_alarm")
    private Integer falseAlarm;

    /**
     *  告警类型，1：雷摄，2；光摄
     */
    @TableField(value = "alarm_type")
    private Integer alarmType;

    /**
     *  处理意见
     */
    @TableField(value = "opinions")
    private String opinions;


}
