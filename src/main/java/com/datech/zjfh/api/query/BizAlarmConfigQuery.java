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
public class BizAlarmConfigQuery implements Serializable{

    private static final long serialVersionUID = 2460036842315284211L;

    private List<Integer> idList;
    private Integer id;
    private List<BizAlarmConfigTime> timeList;

}
