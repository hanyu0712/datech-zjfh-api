package com.datech.zjfh.api.config;

import com.alibaba.fastjson.JSONObject;
import com.datech.zjfh.api.common.ivs.KeepAlive;
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.service.BizIvsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@EnableScheduling
@Configuration
public class IvsKeepAliveTask {

    @Resource
    private BizIvsServiceImpl bizIvsService;

    @Scheduled(cron = "1 0/2 * * * ?")
    public void configureTasks() {
        log.info("====IvsKeepAliveTask");
        List<BizIvsEntity> ivsList = bizIvsService.list();
        for (BizIvsEntity ivs : ivsList) {
            try {
                if (StringUtils.isBlank(ivs.getToken())) {
                    //激活ivs
                    bizIvsService.ivsActivate(ivs);
                } else {
                    String result = KeepAlive.keepAlive("https://" + ivs.getIp() + ":18531", ivs.getToken());
                    if (StringUtils.isNotBlank(result)) {
                        JSONObject a = JSONObject.parseObject(result);
                        if (Integer.parseInt(a.get("resultCode").toString()) != 0) {
                            //激活ivs
                            bizIvsService.ivsActivate(ivs);
                        } else {
                            ivs.setOnLine(1);   //在线
                            bizIvsService.updateById(ivs);
                        }
                    } else {
                        //激活ivs
                        bizIvsService.ivsActivate(ivs);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


}
