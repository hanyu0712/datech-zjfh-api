package com.datech.zjfh.api.config;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.datech.zjfh.api.common.ivs.KeepAlive;
import com.datech.zjfh.api.common.ivs.Login;
import com.datech.zjfh.api.entity.BizAlarmEntity;
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.service.BizAlarmServiceImpl;
import com.datech.zjfh.api.service.BizCameraServiceImpl;
import com.datech.zjfh.api.service.BizIvsServiceImpl;
import com.datech.zjfh.api.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@EnableScheduling
@Configuration
public class IvsKeepAliveTask {

    @Autowired
    private IVS1800Runner iVS1800Runner;
    @Resource
    private BizIvsServiceImpl bizIvsService;

    @Scheduled(cron = "1 0/5 * * * ?")
    public void configureTasks() throws Exception {
        log.info("====IvsKeepAliveTask");
        List<BizIvsEntity> ivsList = bizIvsService.list();
        for (BizIvsEntity ivs : ivsList) {
            try {
                if (StringUtils.isBlank(ivs.getToken())) {
                    //登录
                    String token = Login.loginAndGetToken("https://" + ivs.getIp() + ":18531", ivs.getAccount(), ivs.getPassword());
                    ivs.setToken(token);
                    ivs.setOnLine(1);   //在线
                    bizIvsService.updateById(ivs);
                } else {
                    String result = KeepAlive.keepAlive("https://" + ivs.getIp() + ":18531", ivs.getToken());
                    JSONObject a = JSONObject.parseObject(result);
                    if (Integer.parseInt(a.get("resultCode").toString()) != 0) {
                        ivs.setOnLine(0);   //离线
                        bizIvsService.updateById(ivs);
                    } else {
                        ivs.setOnLine(1);   //在线
                        bizIvsService.updateById(ivs);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
