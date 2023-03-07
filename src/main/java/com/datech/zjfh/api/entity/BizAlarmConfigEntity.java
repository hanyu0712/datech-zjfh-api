package com.datech.zjfh.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("biz_alarm_config")
public class BizAlarmConfigEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 摄像头ID
     */
    @TableField(value = "camera_id")
    private Integer cameraId;
    /**
     * 摄像头IP
     */
    @TableField(value = "camera_ip")
    private String cameraIp;
    /**
     * 摄像头编码
     */
    @TableField(value = "camera_code")
    private String cameraCode;

    /**
     *  关闭告警时间
     */
    @TableField(value = "begin_time")
    private String beginTime;
    /**
     *  开启告警时间
     */
    @TableField(value = "end_time")
    private String endTime;

    /**
     *  状态
     */
    @TableField(value = "state")
    private Integer state;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     *  创建人
     */
    @TableField(value = "create_user")
    private String createUser;

    /**
     * 删除时间
     */
    @TableField(value = "del_time")
    private Date delTime;

    /**
     *  删除人
     */
    @TableField(value = "del_user")
    private String delUser;



}
