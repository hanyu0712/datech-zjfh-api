package com.datech.zjfh.api.common.ivs;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import static com.datech.zjfh.api.common.ivs.CreateSSLClientDefault.createSSLClientDefault;

@Slf4j
public class Ptzcontrol {

    /**
     * 云台控制
     */
    public static String sendRequest(String ivsHost, String cookie, String cameraCode, int index) {
        CloseableHttpClient httpclient = createSSLClientDefault();
        CloseableHttpResponse response = null;
        Gson gson = new Gson();

        HashMap<String, Object> params = new HashMap<>();
        params.put("cameraCode", cameraCode);
        params.put("controlCode", 11);
        params.put("controlPara1", index + "");
        params.put("controlPara2", "");
        String result = "";
        try {
            HttpPost httpPost = new HttpPost(ivsHost + "/device/ptzcontrol");
            //添加头文件
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cache-Control", "no-cache");
            httpPost.addHeader("Cookie", cookie);
            // 添加参数信息
            log.info(gson.toJson(params));
            httpPost.setEntity(new StringEntity(gson.toJson(params), "UTF-8"));
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
            log.info("==========云台控制 response code:{}", response.getStatusLine().getStatusCode());
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("==========云台控制异常：{}", e.getMessage());
        } finally {
            if (null != response) {
                try {
                    response.close();
                    httpclient.close();
                } catch (IOException e) {
                    log.error("==========云台控制异常：释放连接错误:{}", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}


