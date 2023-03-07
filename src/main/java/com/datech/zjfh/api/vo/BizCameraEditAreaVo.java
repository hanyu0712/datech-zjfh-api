package com.datech.zjfh.api.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 摄像头预警区域设置、编辑
 */
@Data
public class BizCameraEditAreaVo {
    private List<Integer> idList = new ArrayList<>();
    private String area;
    private String coordinates;

}
