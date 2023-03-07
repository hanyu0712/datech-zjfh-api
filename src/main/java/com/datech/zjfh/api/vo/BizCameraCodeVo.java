package com.datech.zjfh.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BizCameraCodeVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String code;
    private String name;
    private String ip;
}
