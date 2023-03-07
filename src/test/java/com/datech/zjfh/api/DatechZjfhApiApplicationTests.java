package com.datech.zjfh.api;

import com.datech.zjfh.api.common.ivs.AddIntelligentData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@SpringBootTest
class DatechZjfhApiApplicationTests {

    @Autowired
    public RestTemplate restTemplate;

    @Test
    void contextLoads() {
        String url = "http://jsonplaceholder.typicode.com/posts";
        // 请求头设置,x-www-form-urlencoded格式的数据
        //提交参数设置
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", "zimug 发布文章第二篇");
        map.add("body", "zimug 发布文章第二篇 测试内容");

//        String result = AddIntelligentData.sendRequest(restTemplate, url, "cookie", map);
//        System.out.println(result);
    }

}
