package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.entity.BizAlarmEntity;
import com.datech.zjfh.api.mapper.BizAlarmMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BizAlarmServiceImpl extends ServiceImpl<BizAlarmMapper, BizAlarmEntity> implements PageService<BizAlarmEntity> {


}
