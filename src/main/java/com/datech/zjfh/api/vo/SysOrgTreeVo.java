package com.datech.zjfh.api.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录信息对象
 */
@Data
public class SysOrgTreeVo {
    private Integer id;
    private String name;
    private Integer level;
    private Integer pid;
    private List<SysOrgTreeVo> childList = new ArrayList<>();
    private List<BizCameraCodeVo> cameraList;

}
