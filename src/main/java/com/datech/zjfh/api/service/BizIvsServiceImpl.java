package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.common.ivs.Login;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.mapper.BizIvsMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BizIvsServiceImpl extends ServiceImpl<BizIvsMapper, BizIvsEntity> implements PageService<BizIvsEntity> {


    @Resource
    private BizCameraServiceImpl bizCameraService;

    public List<Integer> getIvsList(Integer lineId) {
        if (lineId == null) return null;
        LambdaQueryWrapper<BizIvsEntity> ivsWrapper = Wrappers.lambdaQuery();
        ivsWrapper.eq(BizIvsEntity::getLineId, lineId);
        List<BizIvsEntity> ivsList = this.list(ivsWrapper);
        return ivsList.stream().map(BizIvsEntity::getId).collect(Collectors.toList());
    }
    public List<Integer> getAllIvsList() {
        LambdaQueryWrapper<BizIvsEntity> ivsWrapper = Wrappers.lambdaQuery();
        List<BizIvsEntity> ivsList = this.list(ivsWrapper);
        return ivsList.stream().map(BizIvsEntity::getId).collect(Collectors.toList());
    }

    //激活ivs
    public void ivsActivate(BizIvsEntity ivs) {
        //登录
        log.info("ivs1800 login ip:{}, account :{}, passowrd :{}", ivs.getIp(), ivs.getAccount(), ivs.getPassword());
        String token = Login.loginAndGetToken("https://" + ivs.getIp() + ":18531", ivs.getAccount(), ivs.getPassword());
        log.info("ivs1800 return : {}", token);
        if (StringUtils.isNotBlank(token)) {
            ivs.setToken(token);
            ivs.setOnLine(1);   //在线
            this.updateById(ivs);
            //订阅告警
            LambdaQueryWrapper<BizCameraEntity> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(BizCameraEntity::getIvsId, ivs.getId());
            queryWrapper.eq(BizCameraEntity::getSubsEnable, 1); //开启订阅
            List<BizCameraEntity> cameraEntityList = bizCameraService.list(queryWrapper);
            for (BizCameraEntity camera : cameraEntityList) {
                bizCameraService.addIntelligentData(camera, ivs);
            }
            bizCameraService.updateBatchById(cameraEntityList);
        }else {
            ivs.setOnLine(0);   //离线
            this.updateById(ivs);
        }
    }

}
