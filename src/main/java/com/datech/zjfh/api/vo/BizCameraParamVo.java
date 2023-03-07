package com.datech.zjfh.api.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 摄像头里程标段、编码、名称查询条件
 */
@Data
public class BizCameraParamVo {
    private Integer orgId;
    private List<Object> areaList = new ArrayList<>();
    private Map<String, List<BizCameraCodeVo>> codeMap = new HashMap<>();

}
