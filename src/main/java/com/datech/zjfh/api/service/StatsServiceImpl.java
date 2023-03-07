package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.datech.zjfh.api.entity.BizAlarmEntity;
import com.datech.zjfh.api.vo.StatsTotalVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class StatsServiceImpl {


    @Resource
    private BizAlarmServiceImpl bizAlarmService;

    public StatsTotalVo getTotal(Date lastBegin, Date lastEnd, Date begin, Date end, Integer lineId) {
        LambdaQueryWrapper<BizAlarmEntity> queryWrapper = getDateQueryWrapper(lastBegin, lastEnd, lineId);
        int totalLast = bizAlarmService.count(queryWrapper);   //上周总数

        queryWrapper.eq(BizAlarmEntity::getState, 1);
        int dealLast = bizAlarmService.count(queryWrapper);    //上周已处理

        queryWrapper = getDateQueryWrapper(begin, end, lineId);
        int total = bizAlarmService.count(queryWrapper);    //本周总数

        queryWrapper.eq(BizAlarmEntity::getState, 1);
        int deal = bizAlarmService.count(queryWrapper); //本周已处理

        queryWrapper.eq(BizAlarmEntity::getFalseAlarm, 0);
        int dealCorrect = bizAlarmService.count(queryWrapper);  //本周正确告警

        queryWrapper = getDateQueryWrapper(begin, end, lineId);
        queryWrapper.eq(BizAlarmEntity::getState, 0);
        int unDeal = bizAlarmService.count(queryWrapper);   //本周未处理

        queryWrapper = getDateQueryWrapper(begin, end, lineId);
        queryWrapper.eq(BizAlarmEntity::getState, 2);
        int ignore = bizAlarmService.count(queryWrapper);   //本周已忽略
        StatsTotalVo vo = new StatsTotalVo();
        vo.setTotalLast(totalLast);
        vo.setDealLast(dealLast);
        vo.setTotal(total);
        vo.setDeal(deal);
        vo.setUnDeal(unDeal);
        vo.setDealCorrect(dealCorrect);
        vo.setIgnore(ignore);
        return vo;
    }

    public LambdaQueryWrapper<BizAlarmEntity> getDateQueryWrapper(Date begin, Date end, Integer lineId) {
        LambdaQueryWrapper<BizAlarmEntity> queryWrapper = Wrappers.lambdaQuery();
        if (lineId != null) {
            queryWrapper.eq(BizAlarmEntity::getLineId, lineId);
        }
        queryWrapper.ge(BizAlarmEntity::getTriggerTime, begin);
        queryWrapper.lt(BizAlarmEntity::getTriggerTime, end);
        return  queryWrapper;
    }
    public LambdaQueryWrapper<BizAlarmEntity> getDayAreaQueryWrapper(Date begin, Date end, List<String> cameraCodeList, Integer lineId) {
        LambdaQueryWrapper<BizAlarmEntity> queryWrapper = getDateQueryWrapper(begin, end, lineId);
        queryWrapper.in(BizAlarmEntity::getCameraCode, cameraCodeList);
        return queryWrapper;
    }

}
