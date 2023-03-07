package com.datech.zjfh.api.common.bean;

import com.datech.zjfh.api.entity.BizCameraEntity;
import lombok.Data;

import java.util.List;

@Data
public class IvsCameraBriefInfos {
    private List<BizCameraEntity> cameraBriefInfoList;
}
