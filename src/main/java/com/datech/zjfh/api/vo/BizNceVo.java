package com.datech.zjfh.api.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BizNceVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer lineId;
    private String lineName;
    private String name;
    private String ip;
    private Float length;
    private String account;
    private String password;
    private Integer state;
    private Integer subsEnable;
}
