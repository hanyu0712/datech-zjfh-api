package com.datech.zjfh.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class BizPresetEditVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer lineId;
    private Integer nceId;
    private String ip;
    private Map<Integer, float[]> presetMap;
}
