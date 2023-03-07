package com.datech.zjfh.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datech.zjfh.api.entity.SysUserEntity;
import com.datech.zjfh.api.query.SysUserQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserEntity> {

    /**
     * 通过用户账号查询用户信息
     * @param username
     * @return
     */
    SysUserEntity getUserByName(@Param("username") String username);

    /**
     * 分页查询
     * @param page
     * @return
     */
    IPage<SysUserEntity> pageList(Page<SysUserEntity> page, @Param(value = "query") SysUserQuery query,
                                  @Param("permissionSql") String permissionSql);

    /*IPage<SysUserEntity> getUserByRoleId(IPage<SysUserEntity> page,@Param("roleId") String roleId,@Param("username") String username);

    List<SysUserOrgVO> getOrgListByUserIds(@Param("userIds") List<String> userIds);*/

}
