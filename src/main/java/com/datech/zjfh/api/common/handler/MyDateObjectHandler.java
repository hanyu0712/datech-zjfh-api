package com.datech.zjfh.api.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;
@Slf4j
@Component
public class MyDateObjectHandler implements MetaObjectHandler {

    //插入时填充策略
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("start  insert  fill.......");
        this.setFieldValByName("createTime",new Date(),metaObject);
        this.setFieldValByName("updateTime",new Date(),metaObject);
    }

    //更新时填充策略
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("start  update  fill.......");
        this.setFieldValByName("updateTime",new Date(),metaObject);
    }
}
