package com.datech.zjfh.api.common.nce.bean;

import lombok.Data;

@Data
public class NceAlarm {

    public float offset;
    public float riskPointDistance;
    public int eventType;
    public int alarmLevel;
    public String eventUUID;
    public String fiberUUID;
    public String neUUID;
    public long firstReportTime;
    public long latestReportTime;
    public long endtime;

}
