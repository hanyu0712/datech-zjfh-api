package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.common.ivs.Login;
import com.datech.zjfh.api.common.ivs.Logout;
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
        List<Integer> ivsIdList = ivsList.stream().map(BizIvsEntity::getId).collect(Collectors.toList());
        return ivsIdList;
    }
    public List<Integer> getAllIvsList() {
        LambdaQueryWrapper<BizIvsEntity> ivsWrapper = Wrappers.lambdaQuery();
        List<BizIvsEntity> ivsList = this.list(ivsWrapper);
        List<Integer> ivsIdList = ivsList.stream().map(BizIvsEntity::getId).collect(Collectors.toList());
        return ivsIdList;
    }


    public void syncIvs1800Camera() {
        List<BizIvsEntity> ivsList = this.list();
        for (BizIvsEntity ivs : ivsList) {
            if (StringUtils.isNotBlank(ivs.getToken())) {
                //注销
                Logout.Logout("https://" + ivs.getIp() + ":18531", ivs.getToken());
            }
            //登录
            String token = Login.loginAndGetToken("https://" + ivs.getIp() + ":18531", ivs.getAccount(), ivs.getPassword());
            log.info("======IVS1800Runner token:{}", token);
            if (StringUtils.isNotBlank(token)) {
                ivs.setToken(token);
                this.updateById(ivs);
                //初始化摄像头,订阅告警
                bizCameraService.syncIvs1800Camera(ivs);
            }
        }
    }
}
