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
import java.text.SimpleDateFormat;
import java.util.Date;
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

    @Scheduled(cron = "0 * * * * ?")
    public void configureTasks() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String nowTime = format.format(new Date());
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
                if (camera != null && StringUtils.isNotBlank(camera.getSubscribeId())) {
                    int resultCode = bizCameraService.deleteIntelligentData(camera.getSubscribeId(), bizIvsService.getById(camera.getIvsId()));
                    if (resultCode == 0) {
                        camera.setSubscribeId("");
                        camera.setSubsEnable(0);
                        bizCameraService.updateById(camera);
                        log.info("摄像头:['" + camera.getDeviceIp() + "']删除订阅告警，时间:" + nowTime +"， 非布防时间段："+config.getBeginTime()+"--"+config.getEndTime());
                    } else {
                        log.info("摄像头:['" + camera.getDeviceIp() + "']删除订阅告警失败！！！！！，resultCode:" + resultCode);
                    }
                }
            }
            if (nowTime.compareTo(config.getEndTime()) >= 0) {
                //当前时间大于等于非布防时间段结束时间，订阅告警
                BizCameraEntity camera = bizCameraService.getById(config.getCameraId());
                if (camera != null && StringUtils.isBlank(camera.getSubscribeId())) {
                    //订阅告警
                    int resultCode = bizCameraService.addIntelligentData(camera, bizIvsService.getById(camera.getIvsId()));
                    if (resultCode == 0) {
                        camera.setSubsEnable(1);
                        bizCameraService.updateById(camera);
                        log.info("摄像头:['" + camera.getDeviceIp() + "']新增订阅告警，时间:" + nowTime + "， 非布防时间段：" + config.getBeginTime() + "--" + config.getEndTime());
                    } else {
                        log.info("摄像头:['" + camera.getDeviceIp() + "']新增订阅告警失败！！！！！，resultCode:" + resultCode);
                    }
                }
            }
        }
    }

}
