/**
 * 
 */
package com.datech.zjfh.api.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysLogQuery implements Serializable{

    private static final long serialVersionUID = 2460036842315284211L;

    /**
     * 登录账号
     */
    private String opUsername;
    private String ip;
    private String createTimeBegin;
    private String createTimeEnd;
    private Integer pageNo;
    private Integer pageSize;
}
