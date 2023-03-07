package com.datech.zjfh.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class BizPresetVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ip;
    private String name;
    private Integer presetCount = 0;
    private Float begin = 0f;
    private Float end = 0f;
    private Map<Integer, float[]> beginEndMap;
}
