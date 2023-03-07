package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.entity.BizLineEntity;
import com.datech.zjfh.api.mapper.BizLineMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BizLineServiceImpl extends ServiceImpl<BizLineMapper, BizLineEntity> implements PageService<BizLineEntity> {


}
