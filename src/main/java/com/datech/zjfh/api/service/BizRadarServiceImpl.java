package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.entity.BizRadarEntity;
import com.datech.zjfh.api.mapper.BizRadarMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BizRadarServiceImpl extends ServiceImpl<BizRadarMapper, BizRadarEntity> implements PageService<BizRadarEntity> {


}
