/**
 * 
 */
package com.datech.zjfh.api.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BizCameraQuery implements Serializable{

    private static final long serialVersionUID = 2460036842315284211L;

    private Integer orgId;
    private String area;
    private String code;
    private String deviceIp;
    private Integer status;
    private Integer subsEnable;
    private Integer pageNo;
    private Integer pageSize;
    private List<Integer> idList;

}
