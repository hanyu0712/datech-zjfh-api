package com.datech.zjfh.api.common.bean;

import lombok.Data;

import java.util.List;

@Data
public class IvsAddIntelligentDataResult {
    public int resultCode;
    public List<IvsAddIntelligentDataResultInfo> resultInfoList;

}
