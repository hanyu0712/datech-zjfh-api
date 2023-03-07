package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.common.bean.PageInfo;
import com.datech.zjfh.api.entity.SysLogEntity;
import com.datech.zjfh.api.mapper.SysLogMapper;
import com.datech.zjfh.api.query.SysLogQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLogEntity> implements PageService<SysLogEntity> {

    @Resource
    private SysLogMapper sysLogMapper;

    public PageInfo<SysLogEntity> selectPage(SysLogQuery query, int pageNum, int pageSize) {
        Page<SysLogEntity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysLogEntity> wrapper = new QueryWrapper<>();

        wrapper.eq(StringUtils.isNotBlank(query.getOpUsername()), "opUsername", query.getOpUsername());
        wrapper.between(StringUtils.isNotBlank(query.getCreateTimeBegin()), "create_time", query.getCreateTimeBegin(), query.getCreateTimeEnd() + " 23:59:59");
        wrapper.orderByDesc("create_time");
        IPage<SysLogEntity> pageList = sysLogMapper.selectPage(page, wrapper);
        return toPageInfo(pageList);
    }


}
