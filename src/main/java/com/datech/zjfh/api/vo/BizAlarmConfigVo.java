package com.datech.zjfh.api.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BizAlarmConfigVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String beginTime;
    private String endTime;
}
