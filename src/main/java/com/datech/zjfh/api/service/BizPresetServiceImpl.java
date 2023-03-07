package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.common.ivs.GetPresetList;
import com.datech.zjfh.api.entity.BizAlarmEntity;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.entity.BizPresetEntity;
import com.datech.zjfh.api.mapper.BizPresetMapper;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BizPresetServiceImpl extends ServiceImpl<BizPresetMapper, BizPresetEntity> implements PageService<BizPresetEntity> {

    @Resource
    private LogUtil logUtil;
    @Resource
    private BizPresetMapper bizPresetMapper;

    public BizPresetEntity getByDistance(Integer nceId, Float distance) {
        LambdaQueryWrapper<BizPresetEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.le(BizPresetEntity::getBegin, distance);
        queryWrapper.ge(BizPresetEntity::getEnd, distance);
        queryWrapper.eq(BizPresetEntity::getNceId, nceId);
        List<BizPresetEntity> tempList = bizPresetMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(tempList)) {
            return tempList.get(0);
        }
        return null;
    }

    public void presetReload(BizCameraEntity camera, BizIvsEntity ivs) {
        boolean delete = false;
        List<BizPresetEntity> ivsPresetList = GetPresetList.getPresetList("https://" + ivs.getIp() + ":18531", ivs.getToken(), camera.getCode(), camera.getDomainCode());
        LambdaQueryWrapper<BizPresetEntity> presetWrapper = Wrappers.lambdaQuery();
        presetWrapper.eq(BizPresetEntity::getCameraIp, camera.getDeviceIp());
        int count = this.count(presetWrapper);
        if (count == ivsPresetList.size()) {
            for (BizPresetEntity ivsPreset : ivsPresetList) {
                presetWrapper = Wrappers.lambdaQuery();
                presetWrapper.eq(BizPresetEntity::getCameraIp, camera.getDeviceIp());
                presetWrapper.eq(BizPresetEntity::getPresetName, ivsPreset.getPresetName());
                presetWrapper.eq(BizPresetEntity::getPresetIndex, ivsPreset.getPresetIndex());
                if (this.count(presetWrapper) == 0) {
                    presetWrapper = Wrappers.lambdaQuery();
                    presetWrapper.eq(BizPresetEntity::getCameraIp, camera.getDeviceIp());
                    this.remove(presetWrapper);
                    delete = true;
                    break;
                }
            }
        } else {
            this.remove(presetWrapper);
            delete = true;
        }
        if (delete) {
            for (BizPresetEntity preset : ivsPresetList) {
                preset.setCameraIp(camera.getDeviceIp());
                preset.setCameraCode(camera.getCode() + "#" + camera.getDomainCode());
                preset.setCameraName(camera.getName());
                this.save(preset);
            }
        }
    }


}
