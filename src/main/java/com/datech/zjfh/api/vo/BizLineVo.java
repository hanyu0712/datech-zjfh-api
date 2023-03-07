package com.datech.zjfh.api.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BizLineVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String no;
    private String name;
    private Integer cameraCount;
    private Float length;
}
