package com.datech.zjfh.api.config;


import com.datech.zjfh.api.common.ivs.Login;
import com.datech.zjfh.api.common.ivs.Logout;
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.service.BizCameraServiceImpl;
import com.datech.zjfh.api.service.BizIvsServiceImpl;
import com.datech.zjfh.api.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
@Order(value = 1)
public class IVS1800Runner implements ApplicationRunner {

    @Resource
    private BizIvsServiceImpl bizIvsService;


    @Override
    public void run(ApplicationArguments args){
        try {
            bizIvsService.syncIvs1800Camera();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


}
