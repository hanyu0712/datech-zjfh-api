package com.datech.zjfh.api.common.ivs;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.datech.zjfh.api.common.ivs.CreateSSLClientDefault.createSSLClientDefault;

@Slf4j
public class DeleteIntelligentData {
    /**
     * 通过httppost方式调用接口删除智能数据订阅
     */
    public static String deleteIntelligentData(String url, String cookie, String subscribeId) {
        CloseableHttpClient httpclient = createSSLClientDefault();
        CloseableHttpResponse response = null;
        Gson gson = new Gson();

        String result = "";
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("SubscribeID", subscribeId);

            List<HashMap<String, String>> subscribeObject = new ArrayList<>();
            subscribeObject.add(params);

            //将完整请求参数放入body
            HashMap<String, List<HashMap<String, String>>> body = new HashMap<>();
            body.put("SubscribeIDList", subscribeObject);
            log.info("=========deleteIntelligentData:" + JSONObject.toJSONString(body));
            HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url + "/sdk_service/rest/subscribes");
            //添加头文件
            httpDelete.addHeader("Content-Type", "application/json");
            httpDelete.addHeader("Cache-Control", "no-cache");
            httpDelete.addHeader("Cookie", cookie);
            // 添加参数信息

            httpDelete.setEntity(new StringEntity(gson.toJson(body), "UTF-8"));
            //将请求发送给1800并获取返回结果
            response = httpclient.execute(httpDelete);
            //获取返回结果并解析
            InputStream content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(content, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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


}


