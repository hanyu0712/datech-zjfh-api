package com.datech.zjfh.api.vo;

import lombok.Data;

import java.util.List;

/**
 * 摄像头预警区域设置、编辑
 */
@Data
public class BizCameraEditVo {
    private Integer orgId;
    private Integer lineId;
    private Integer alarmType;
    private List<BizCameraEditAreaVo> areaVoList;
}
