package com.datech.zjfh.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class BizPresetUnbindVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Set<String> ipList;
    private Map<String, List<Integer>> presetMap;
}
