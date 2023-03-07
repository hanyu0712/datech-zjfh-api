/**
 * 
 */
package com.datech.zjfh.api.query;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
//@EqualsAndHashCode(callSuper = false)
//@Accessors(chain = true)
public class BizAlarmConfigTime implements Serializable{

    private static final long serialVersionUID = 2460036842315284211L;

    private String beginTime;
    private String endTime;

}
