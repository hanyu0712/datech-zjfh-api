package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.entity.BizAlarmConfigEntity;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.mapper.BizAlarmConfigMapper;
import com.datech.zjfh.api.query.BizAlarmConfigQuery;
import com.datech.zjfh.api.query.BizAlarmConfigTime;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.LoginUtil;
import com.datech.zjfh.api.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class BizAlarmConfigServiceImpl extends ServiceImpl<BizAlarmConfigMapper, BizAlarmConfigEntity> {

    @Autowired
    private LogUtil logUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizIvsServiceImpl bizIvsService;

    /**
     * 非布防时间段为空时，只删除现有配置，并订阅告警
     */
    public Result<Object> addBatch(BizAlarmConfigQuery param) {
        if (param == null || CollectionUtils.isEmpty(param.getIdList())) {
            return Result.error("必填参数为空");
        }
        if (CollectionUtils.isNotEmpty(param.getTimeList())) {
            for (BizAlarmConfigTime configTime : param.getTimeList()) {
                if (StringUtils.isBlank(configTime.getBeginTime()) && StringUtils.isBlank(configTime.getEndTime())) {
                    return Result.error("非布防时间参数有空值");
                }
            }
        }
        if (!checkParamTime(param.getTimeList())) {
            return Result.error("非布防时间参数冲突");
        }
        String realName = getRealName();
        // 逻辑删除原有配置
        deleteOriConfig(param.getIdList(), realName);
        // 新增配置
        for (Integer cameraId : param.getIdList()) {
            BizCameraEntity camera = bizCameraService.getById(cameraId);
            if (camera != null) {
                boolean subscribe = doAdd(param.getTimeList(), camera, realName);
                // 更新摄像头布防状态
                doSubscribe(subscribe, camera);
            }else {
                return Result.error("操作失败，摄像头不存在");
            }
        }
        return Result.OK();
    }

    /**
     * 非布防时间段为空时，只删除现有配置，并订阅告警
     */
    public Result<Object> add(BizAlarmConfigQuery param) {
        if (param == null || param.getId() == null) {
            return Result.error("必填参数为空");
        }
        if (!checkParamTime(param.getTimeList())) {
            return Result.error("非布防时间参数冲突");
        }
        String realName = getRealName();
        // 逻辑删除原有配置
        deleteOriConfig(Collections.singletonList(param.getId()), realName);
        // 新增配置
        BizCameraEntity camera = bizCameraService.getById(param.getId());
        if (camera != null) {
            boolean subscribe = doAdd(param.getTimeList(), camera, realName);
            // 更新摄像头布防状态
            doSubscribe(subscribe, camera);
            return Result.OK();
        } else {
            return Result.error("操作失败，摄像头不存在");
        }
    }

    private boolean deleteOriConfig(List<Integer> cameraIdList, String realName) {
        BizAlarmConfigEntity updateEntity = new BizAlarmConfigEntity();
        updateEntity.setState(1);//删除
        updateEntity.setDelTime(new Date());
        updateEntity.setDelUser(realName);
        LambdaUpdateWrapper<BizAlarmConfigEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(BizAlarmConfigEntity::getCameraId, cameraIdList);
        updateWrapper.eq(BizAlarmConfigEntity::getState, 0);
        return this.update(updateEntity, updateWrapper);
    }

    public boolean deleteByCameraIp(List<String> cameraIpList, String realName) {
        BizAlarmConfigEntity updateEntity = new BizAlarmConfigEntity();
        updateEntity.setState(1);//删除
        updateEntity.setDelTime(new Date());
        updateEntity.setDelUser(realName);
        LambdaUpdateWrapper<BizAlarmConfigEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(BizAlarmConfigEntity::getCameraIp, cameraIpList);
        updateWrapper.eq(BizAlarmConfigEntity::getState, 0);
        return this.update(updateEntity, updateWrapper);
    }

    private void doSubscribe(boolean subscribe, BizCameraEntity camera) {
        if (camera != null) {
            // 判断是否需要订阅告警
            if (subscribe) {
                //订阅告警
                camera.setSubsEnable(1);//开启订阅告警标志
                bizCameraService.addIntelligentData(camera, bizIvsService.getById(camera.getIvsId()));
            } else {
                if (StringUtils.isNotBlank(camera.getSubscribeId())) {
                    //删除订阅告警
                    int resultCode = bizCameraService.deleteIntelligentData(camera.getSubscribeId(), bizIvsService.getById(camera.getIvsId()));
                    if (resultCode == 0) {
                        camera.setSubscribeId("");
                        camera.setSubsEnable(0); //关闭订阅告警标志
                    }
                }
            }
            bizCameraService.updateById(camera);
        }
    }

    private boolean doAdd(List<BizAlarmConfigTime> timeList, BizCameraEntity camera, String realName) {
        if(CollectionUtils.isEmpty(timeList)) {
            return true;
        }
        // 是否需要订阅告警
        boolean subscribe = true;
        //当前时间
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String nowTime = format.format(new Date());
        for (BizAlarmConfigTime time : timeList) {
            BizAlarmConfigEntity entity = new BizAlarmConfigEntity();
            entity.setCameraId(camera.getId());
            entity.setCameraIp(camera.getDeviceIp());
            entity.setCameraCode(camera.getCode());
            entity.setBeginTime(time.getBeginTime());
            entity.setEndTime(time.getEndTime());
            entity.setState(0);
            entity.setCreateUser(realName);
            this.save(entity);
            if ((nowTime.compareTo(time.getBeginTime()) >= 0 && nowTime.compareTo(time.getEndTime()) < 0))
                subscribe = false;
        }
        return subscribe;
    }

//    public String getNowTime() {
//        StringBuilder nowTime = new StringBuilder();
//        Calendar today = Calendar.getInstance();
//        nowTime.append(today.get(Calendar.HOUR_OF_DAY));
//        nowTime.append(":");
//        nowTime.append(today.get(Calendar.MINUTE));
//        nowTime.append(":00");
//        log.info("nowTime:{}", nowTime);
//        return nowTime.toString();
//    }

    public boolean checkParamTime(List<BizAlarmConfigTime> timeList) {
        if (CollectionUtils.isEmpty(timeList))
            return true;
        for (int i = 0; i < timeList.size(); i++) {
            BizAlarmConfigTime time = timeList.get(i);
            if (time.getBeginTime().compareTo(time.getEndTime()) >= 0)
                return false;

            for (int j = i + 1; j < timeList.size(); j++) {
                BizAlarmConfigTime time2 = timeList.get(j);
                if (!((time.getBeginTime().compareTo(time2.getBeginTime()) < 0 && time.getEndTime().compareTo(time2.getBeginTime()) < 0)
                        || (time.getBeginTime().compareTo(time2.getEndTime()) > 0 && time.getEndTime().compareTo(time2.getEndTime()) > 0)))
                    return false;
            }
        }
        return true;
    }


    public String getRealName() {
        LoginUser loginUser = LoginUtil.getLoginUser(request.getHeader("token"), redisUtil);
        return loginUser == null ? null : loginUser.getRealname();
    }

}
