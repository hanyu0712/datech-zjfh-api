package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.entity.BizCameraAlarmEntity;
import com.datech.zjfh.api.mapper.BizCameraAlarmMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BizCameraAlarmServiceImpl extends ServiceImpl<BizCameraAlarmMapper, BizCameraAlarmEntity> implements PageService<BizCameraAlarmEntity> {
  /*  @Resource
    private LogUtil logUtil;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private SysOrgServiceImpl sysOrgService;
    @Resource
    private BizCameraServiceImpl bizCameraServiceImpl;

    public boolean add(BizCameraEntity camera) {
        BizCameraAlarmEntity alarm = new BizCameraAlarmEntity();
        alarm.setOrgId(camera.getOrgId());
        alarm.setDetail("摄像头离线连接异常");
        alarm.setCameraId(camera.getId());
        alarm.setArea(camera.getArea());
        alarm.setCameraCode(camera.getCode());
        alarm.setCameraName(camera.getName());
        alarm.setCameraIp(camera.getDeviceIp());
        alarm.setState(0);
        alarm.setOrgName(sysOrgService.getOrgFullName(camera.getOrgId()));
        return this.save(alarm);
    }*/

/*
    public void checkOffline() {
        // Object tokenObj = redisUtil.get("ivs1800token");
        if (tokenObj == null) {
            log.error("====checkOffline token is null");
            return;
        }
        // 查询IVS摄像头
        List<BizCameraEntity> cameraList = GetSubDeviceList.getSubDeviceList(url, tokenObj.toString());
        if (cameraList != null && cameraList.size() > 0) {
            List<BizCameraEntity> databaseList = bizCameraServiceImpl.list();
            Map<String, BizCameraEntity> databaseMap = databaseList.stream().collect(Collectors.toMap(BizCameraEntity::getCode, Function.identity()));
            for (BizCameraEntity o : cameraList) {
                //离线摄像头
                if (o.getStatus() == 0) {
                    if (databaseMap.get(o.getCode()) != null) {
                        //数据已存在
                        BizCameraEntity entityUpdate = new BizCameraEntity();
                        //更新离线
                        entityUpdate.setStatus(0);
                        LambdaUpdateWrapper<BizCameraEntity> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(BizCameraEntity::getCode, o.getCode());
                        bizCameraServiceImpl.update(entityUpdate, updateWrapper);
                        //生成设备离线告警
                        add(databaseMap.get(o.getCode()));
                    }
                }
            }
        }
    }*/


}
