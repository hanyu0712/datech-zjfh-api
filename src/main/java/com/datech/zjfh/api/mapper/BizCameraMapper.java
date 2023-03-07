package com.datech.zjfh.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.datech.zjfh.api.entity.BizCameraEntity;

import java.util.List;

public interface BizCameraMapper extends BaseMapper<BizCameraEntity> {

    public int saveBatchXml(List<BizCameraEntity > list);
}
