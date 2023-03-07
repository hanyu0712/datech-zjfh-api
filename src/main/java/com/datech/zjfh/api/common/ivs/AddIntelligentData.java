package com.datech.zjfh.api.common.ivs;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.datech.zjfh.api.common.ivs.CreateSSLClientDefault.createSSLClientDefault;

@Slf4j
public class AddIntelligentData {

    /**
     * 通过httppost方式调用接口添加智能数据订阅
     */
    public static String sendRequest(RestTemplate restTemplate, String url, String cookie, String cameraCode, String receiveAddr) {
        CloseableHttpClient httpclient = createSSLClientDefault();
        CloseableHttpResponse response = null;
        Gson gson = new Gson();

        String result = "";
        try {
            HashMap params = new HashMap();
            HashMap subscribeListObject = new HashMap();
            List subscribeObject = new ArrayList();
            HashMap body = new HashMap();
            // 订阅类别, 可同时带多个类别(必填)
            params.put("SubscribeDetail", "0");
            // 告警信息接收地址，只支持HTTPS URL, 只支持IP，不支持域名。取值范围：数字或字符，字节数不超过512(必填)
            params.put("ReceiveAddr", receiveAddr);
            // 设备编码类型(默认为0)：0-平台编码 1-互联编码。(非必填)
            params.put("CodeType", 0);
            // 回调结果是否需要图片（base64编码）。0-不要图(默认) 1-要全景图 2-要抠图或特写图 3-要全景图和抠图或特写图
            params.put("ResultImgType", 3);
            // 订阅资源路径。支持批量和单个订阅(必填)
            params.put("ResourceURI", cameraCode);
            subscribeObject.add(params);
            subscribeListObject.put("SubscribeObject", subscribeObject);
            //将完整请求参数放入body
            body.put("SubscribeListObject", subscribeListObject);


//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.setCacheControl(CacheControl.noCache());
//            headers.set("Cookie", cookie);
//
//            // 组装请求体
//            HttpEntity<Map> request = new HttpEntity<>(params, headers);
//            // 发送post请求，并打印结果，以String类型接收响应结果JSON字符串
//            result = restTemplate.postForObject(url, request, String.class);

            HttpPost httpPost = new HttpPost(url + "/sdk_service/rest/subscribes");
            //添加头文件
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cache-Control", "no-cache");
            httpPost.addHeader("Cookie", cookie);
            // 添加参数信息

            httpPost.setEntity(new StringEntity(gson.toJson(body), "UTF-8"));
            //将请求发送给1800并获取返回结果
            response = httpclient.execute(httpPost);
            //获取返回结果并解析
            InputStream content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(content, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("订阅告警异常：{}", e.getMessage());
        } finally {
            if (null != response) {
                try {
                    response.close();
                    httpclient.close();
                } catch (IOException e) {
                    System.err.println("释放连接错误");
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 订阅告警
     *
     * @return 订阅ID
     */
    /*public static int sendRequest(BizCameraEntity camera) {
        // Object tokenObj = redisUtil.get("ivs1800token");
        HashMap params = new HashMap();
        HashMap subscribeListObject = new HashMap();
        List subscribeObject = new ArrayList();
        HashMap body = new HashMap();
        // 订阅类别, 可同时带多个类别(必填)
        params.put("SubscribeDetail", "0");

        // 告警信息接收地址，只支持HTTPS URL, 只支持IP，不支持域名。取值范围：数字或字符，字节数不超过512(必填)
        params.put("ReceiveAddr", receiveAddr);
        // 设备编码类型(默认为0)：0-平台编码 1-互联编码。(非必填)
        params.put("CodeType", 0);
        // 回调结果是否需要图片（base64编码）。0-不要图(默认) 1-要全景图 2-要抠图或特写图 3-要全景图和抠图或特写图
        params.put("ResultImgType", 3);

        subscribeObject.add(params);
        subscribeListObject.put("SubscribeObject", subscribeObject);
        //将完整请求参数放入body
        body.put("SubscribeListObject", subscribeListObject);
        // 订阅资源路径。支持批量和单个订阅(必填)
        params.put("ResourceURI", camera.getCode());
        String addResult = AddIntelligentData.coreSendRequest(url + "/sdk_service/rest/subscribes", tokenObj.toString(), body);
        log.info("订阅结果：{}", addResult);
        IvsAddIntelligentDataResult ivsResultList = JSONObject.parseObject(addResult, IvsAddIntelligentDataResult.class);
        if (ivsResultList.getResultCode() == 0 && ivsResultList.resultInfoList.get(0).getResult() == 0) {
            camera.setSubscribeId(ivsResultList.resultInfoList.get(0).getSubscribeID());
            return 0;
        } else {
            if (ivsResultList.getResultCode() != 0) {
                log.error("------摄像头[{}]订阅告警失败：{}, {}", camera.getCode(), ivsResultList.getResultCode(), "null");
                return ivsResultList.getResultCode();
            } else {
                log.error("------摄像头[{}]订阅告警失败：{}, {}", camera.getCode(), ivsResultList.getResultCode(), ivsResultList.resultInfoList.get(0).getResult());
                return ivsResultList.resultInfoList.get(0).getResult();
            }
        }
    }*/


    public static void main(String[] args) {
        // 请求路径
        String url = "https://90.56.29.5:18531/sdk_service/rest/subscribes";
        // 调用login接口获取
        String token = "JSESSIONID=v8iSX3AhIzmLGIdCrzvyqh4EqJtWGYPB";
        HashMap params = new HashMap();
        HashMap subscribeListObject = new HashMap();
        List subscribeObject = new ArrayList();
        HashMap body = new HashMap();
        // 订阅类别, 可同时带多个类别(必填)
        params.put("SubscribeDetail", "85");
        // 订阅资源路径。支持批量和单个订阅(必填)
        params.put("ResourceURI", "00057524295268260101");
        // 告警信息接收地址，只支持HTTPS URL, 只支持IP，不支持域名。取值范围：数字或字符，字节数不超过512(必填)
        params.put("ReceiveAddr", "https://90.253.28.170:443/api/ai/truck");
        // 设备编码类型(默认为0)：0-平台编码 1-互联编码。(非必填)
        params.put("CodeType", 0);
        // 回调结果是否需要图片（base64编码）。0-不要图(默认) 1-要全景图 2-要抠图或特写图 3-要全景图和抠图或特写图
        params.put("ResultImgType", 3);

        subscribeObject.add(params);
        subscribeListObject.put("SubscribeObject", subscribeObject);
        //将完整请求参数放入body
        body.put("SubscribeListObject", subscribeListObject);
        // 发送请求
//        String result = sendRequest(url, token, body);
//        System.out.println(result);
    }
}


