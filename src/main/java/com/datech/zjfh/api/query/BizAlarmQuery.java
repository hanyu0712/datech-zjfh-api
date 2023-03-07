/**
 * 
 */
package com.datech.zjfh.api.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
//@EqualsAndHashCode(callSuper = false)
//@Accessors(chain = true)
public class BizAlarmQuery implements Serializable{

    private static final long serialVersionUID = 2460036842315284211L;

    private Integer orgId;
    private String area;
    private String cameraCode;
    private Integer state;
    private Integer pageNo;
    private Integer pageSize;
    private String beginTime;
    private String endTime;

}
