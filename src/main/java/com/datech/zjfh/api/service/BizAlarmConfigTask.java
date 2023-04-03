package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.datech.zjfh.api.entity.BizAlarmConfigEntity;
import com.datech.zjfh.api.entity.BizCameraEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.List;

@Slf4j
@EnableScheduling
@Configuration
public class BizAlarmConfigTask {

    @Resource
    private BizAlarmConfigServiceImpl bizAlarmConfigService;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizIvsServiceImpl bizIvsService;

    @Scheduled(cron = "1 * * * * ?")
    public void configureTasks() {

        String nowTime = getNowTime();
        log.info("====BizAlarmConfigTask, nowTime:{}", nowTime);
        LambdaQueryWrapper<BizAlarmConfigEntity> configQuery = Wrappers.lambdaQuery();
        configQuery.eq(BizAlarmConfigEntity::getState, 0);
        List<BizAlarmConfigEntity> configList = bizAlarmConfigService.list(configQuery);
        if (CollectionUtils.isEmpty(configList)) {
            return;
        }
        for (BizAlarmConfigEntity config : configList) {

            if (nowTime.compareTo(config.getBeginTime()) >= 0 && nowTime.compareTo(config.getEndTime()) < 0) {
                //在非布防时间内
                BizCameraEntity camera = bizCameraService.getById(config.getCameraId());
                if (StringUtils.isNotBlank(camera.getSubscribeId())) {
                    int resultCode = bizCameraService.deleteIntelligentData(camera.getSubscribeId(), bizIvsService.getById(camera.getIvsId()));
                    if (resultCode == 0) {
                        camera.setSubscribeId("");
                        camera.setSubsEnable(0);
                        bizCameraService.updateById(camera);
                        log.info("摄像头:['" + camera.getId() + "']删除订阅告警，时间:" + nowTime +"， 非布防时间段："+config.getBeginTime()+"--"+config.getEndTime());
                    }
                }
            }
            if (nowTime.compareTo(config.getEndTime()) == 0) {
                //当前时间等于非布防时间段结束时间，订阅告警
                BizCameraEntity camera = bizCameraService.getById(config.getCameraId());
                if (camera != null) {
                    //订阅告警
                    int resultCode = bizCameraService.addIntelligentData(camera, bizIvsService.getById(camera.getIvsId()));
                    if (resultCode == 0) {
                        camera.setSubsEnable(1);
                        bizCameraService.updateById(camera);
                        log.info("摄像头:['" + camera.getId() + "']新增订阅告警，时间:" + nowTime +"， config："+config.getBeginTime()+"--"+config.getEndTime());
                    }
                }
            }
        }
    }

    private String getNowTime() {
        StringBuilder nowTime = new StringBuilder();
        Calendar today = Calendar.getInstance();
        nowTime.append(today.get(Calendar.HOUR_OF_DAY));
        nowTime.append(":");
        if (today.get(Calendar.MINUTE) < 10) {
            nowTime.append("0");
        }
        nowTime.append(today.get(Calendar.MINUTE));
//        log.info("nowTime:{}", nowTime);
        return nowTime.toString();
    }
}
