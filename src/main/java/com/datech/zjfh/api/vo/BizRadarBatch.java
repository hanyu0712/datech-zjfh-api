package com.datech.zjfh.api.vo;

import com.datech.zjfh.api.entity.BizRadarEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BizRadarBatch implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<BizRadarEntity> radarList;
}
