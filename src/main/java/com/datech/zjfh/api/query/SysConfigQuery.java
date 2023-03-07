/**
 * 
 */
package com.datech.zjfh.api.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysConfigQuery implements Serializable{

    private static final long serialVersionUID = 2460036842315284211L;

    private String code;
    private Integer pageNo;
    private Integer pageSize;
}
