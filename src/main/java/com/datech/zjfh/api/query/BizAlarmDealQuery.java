/**
 * 
 */
package com.datech.zjfh.api.query;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
//@EqualsAndHashCode(callSuper = false)
//@Accessors(chain = true)
public class BizAlarmDealQuery implements Serializable{

    private static final long serialVersionUID = 2460036842315284211L;

    private List<Integer> idList;
    private String opinions;
    private Integer falseAlarm;

}
