package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.entity.BizNceEntity;
import com.datech.zjfh.api.mapper.BizNceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BizNceServiceImpl extends ServiceImpl<BizNceMapper, BizNceEntity> implements PageService<BizNceEntity> {


}
