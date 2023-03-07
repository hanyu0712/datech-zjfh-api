package com.datech.zjfh.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.common.bean.Result;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.entity.SysOrgEntity;
import com.datech.zjfh.api.mapper.BizCameraMapper;
import com.datech.zjfh.api.mapper.SysOrgMapper;
import com.datech.zjfh.api.util.BeanCopierUtil;
import com.datech.zjfh.api.vo.SysOrgTreeVo;
import com.datech.zjfh.api.vo.SysOrgVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SysOrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrgEntity> implements PageService<SysOrgEntity> {

    @Resource
    public BizCameraServiceImpl bizCameraService;

    public List<Integer> getOrgAndChildIdList(String orgName) {
        List<Integer> orgIdList = new ArrayList<>();
        if (StringUtils.isNotBlank(orgName)) {
            LambdaQueryWrapper<SysOrgEntity> orgQueryWrapper = Wrappers.lambdaQuery();
            orgQueryWrapper.eq(SysOrgEntity::getName, orgName);
            List<SysOrgEntity> orgList = this.list(orgQueryWrapper);
            if (CollectionUtils.isNotEmpty(orgList)){
                SysOrgEntity org = orgList.get(0);
                orgIdList.add(org.getId());
                getChildIdList(org.getId(), orgIdList);
            }
        }
        return orgIdList;
    }

    public void getChildIdList(Integer pid, List<Integer> orgIdList) {
        LambdaQueryWrapper<SysOrgEntity> orgQueryWrapper = Wrappers.lambdaQuery();
        orgQueryWrapper.eq(SysOrgEntity::getPid, pid);
        List<SysOrgEntity> orgChildList = this.list(orgQueryWrapper);
        if (CollectionUtils.isEmpty(orgChildList)){
            return;
        } else {
            for (SysOrgEntity child : orgChildList) {
                orgIdList.add(child.getId());
                getChildIdList(child.getId(), orgIdList);
            }
        }
    }
    public String getOrgFullName(Integer orgId) {
        String fullName = getNameRecursion(orgId);
        if (StringUtils.isNotBlank(fullName)) {
            fullName = fullName.substring(0, fullName.length() - 1);
        }
        return fullName;
    }

    private String getNameRecursion(Integer orgId) {
        if (orgId == null)
            return "";
        LambdaQueryWrapper<SysOrgEntity> orgQueryWrapper = Wrappers.lambdaQuery();
        orgQueryWrapper.eq(SysOrgEntity::getId, orgId);
        SysOrgEntity org = this.getOne(orgQueryWrapper);
        if (org != null ) {
            return getNameRecursion(org.getPid()) + org.getName() + "-";
        }
        return "";
    }
    public SysOrgTreeVo tree() {
        LambdaQueryWrapper<SysOrgEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByAsc(SysOrgEntity::getLevel);
        List<SysOrgEntity> list = this.list(queryWrapper);

        SysOrgTreeVo root = new SysOrgTreeVo();
        Map<Integer, SysOrgTreeVo> map = new HashMap<>();
        for (SysOrgEntity entity :list) {
            SysOrgTreeVo vo = BeanCopierUtil.copyBean(entity, SysOrgTreeVo.class);
            map.put(vo.getId(), vo);
            Integer parentId = vo.getPid();
            if (vo.getId() == 1){
                root = vo;
            } else if (map.containsKey(parentId)){
                map.get(parentId).getChildList().add(vo);
            }
        }
        return root;
    }

    public boolean save(SysOrgVo vo) {
        Integer pid = vo.getPid();
        SysOrgEntity entity = new SysOrgEntity();
        entity.setName(vo.getName());
        entity.setDescription(vo.getDescription());
        entity.setResponsible(vo.getResponsible());
        entity.setPid(pid);
        entity.setLevel(getById(pid).getLevel() + 1);
        return this.save(entity);
    }

    public boolean deepDelete(Integer id) {
        LambdaQueryWrapper<SysOrgEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysOrgEntity::getPid, id);
        List<SysOrgEntity> childList = this.list(queryWrapper);
        if (childList != null && childList.size() > 0) {
            for (SysOrgEntity org : childList) {
                deepDelete(org.getId());
            }
        }
        UpdateWrapper<BizCameraEntity> updateWrapper = new UpdateWrapper<>();
        //可将指定字段更新为null
        updateWrapper.set("org_id", null);
        updateWrapper.eq("org_id", id);
        bizCameraService.update(updateWrapper);
        return this.removeById(id);
    }


}
