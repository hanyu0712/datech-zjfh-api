package com.datech.zjfh.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_config")
public class SysConfigEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 配置项编码
     */
    @TableField(value = "item_code")
    private String itemCode;


    /**
     * 配置项键
     */
    @TableField(value = "item_key")
    private String itemKey;

    /**
     * 配置项值
     */
    @TableField(value = "item_value")
    private String itemValue;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
