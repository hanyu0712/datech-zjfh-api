package com.datech.zjfh.api.config;


import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.service.BizIvsServiceImpl;
import lombok.extern.slf4j.Slf4j;
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
    public void run(ApplicationArguments args) {
        List<BizIvsEntity> ivsList = bizIvsService.list();
        for (BizIvsEntity ivs : ivsList) {
            try {
                bizIvsService.ivsActivate(ivs);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


}
