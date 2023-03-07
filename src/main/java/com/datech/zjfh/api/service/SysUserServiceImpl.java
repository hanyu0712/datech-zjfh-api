package com.datech.zjfh.api.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.common.bean.OrderBy;
import com.datech.zjfh.api.common.bean.PageInfo;
import com.datech.zjfh.api.common.consts.SystemConstant;
import com.datech.zjfh.api.common.query.QueryGenerator;
import com.datech.zjfh.api.entity.SysUserEntity;
import com.datech.zjfh.api.mapper.SysUserMapper;
import com.datech.zjfh.api.query.SysUserQuery;
import com.datech.zjfh.api.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 用户管理
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements PageService<SysUserEntity> {

    @Resource
    private SysUserMapper sysUserMapper;

    public boolean saveEntity(SysUserEntity entity, LoginUser operator) {
        //获得随机盐值
        String salt = RandomUtil.randomString(SystemConstant.BASE_CODE, 8);
        String password = entity.getPassword();
        entity.setSalt(salt);
        entity.setPassword(PasswordUtil.encrypt(SystemConstant.PROJECT_NAME, password, salt));
        if (operator != null) {
            entity.setCreateBy(operator.getUsername());
            entity.setUpdateBy(operator.getUsername());
        }
        entity.setDelFlag(0);
        return this.save(entity);
    }

    public boolean updatePassword(SysUserEntity entity, LoginUser operator) {
        String password = entity.getPassword();
        entity.setPassword(PasswordUtil.encrypt(SystemConstant.PROJECT_NAME, password, entity.getSalt()));
        if (operator != null) {
            entity.setUpdateBy(operator.getUsername());
        }
        return this.updateById(entity);
    }

    public PageInfo<SysUserEntity> pageList(SysUserQuery query,
                                            int pageNum, int pageSize) {
        Page<SysUserEntity> page = new Page<>(pageNum, pageSize);
        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(SysUserEntity.class);
        OrderBy orderBy = new OrderBy("CREATETIME","desc");
        //此处需要对orderBy进行null判断，如多个OrderBy需对进行遍历顺序插入
        if (orderBy != null && StringUtils.isNotBlank(orderBy.getColumn())) {
            ColumnCache columnCache = columnMap.get(LambdaUtils.formatKey(orderBy.getColumn()));
            if (columnCache != null) {
                page.setOrders(Collections.singletonList(new OrderItem(columnCache.getColumn(), orderBy.isAsc())));
            }
        }
        String sql = QueryGenerator.installAuthJdbc(SysUserEntity.class);
        IPage<SysUserEntity> pageList = sysUserMapper.pageList(page, query, sql);
        return toPageInfo(pageList, Collections.singletonList(orderBy));
    }


}
