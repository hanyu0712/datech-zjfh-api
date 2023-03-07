package com.datech.zjfh.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("biz_preset")
public class BizPresetEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * NCE id
     */
    @TableField(value = "nce_id")
    private Integer nceId;

    /**
     *  摄像机IP
     */
    @TableField(value = "camera_ip")
    private String cameraIp;

    /**
     *  摄像机名称
     */
    @TableField(value = "camera_name")
    private String cameraName;

    /**
     *  摄像机编码
     */
    @TableField(value = "camera_code")
    private String cameraCode;
    /**
     *  预置位名称
     */
    @TableField(value = "preset_name")
    private String presetName;

    /**
     *  预置位编码
     */
    @TableField(value = "preset_index")
    private Integer presetIndex;

    /**
     *  起始距离
     */
    @TableField(value = "begin")
    private Float begin;

    /**
     *  结束距离
     */
    @TableField(value = "end")
    private Float end;


}
