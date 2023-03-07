package com.datech.zjfh.api.common.nce;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.datech.zjfh.api.common.nce.bean.NceAddSubscriptionOutput;
import com.datech.zjfh.api.common.nce.bean.NceAddSubscriptionResult;
import com.datech.zjfh.api.entity.BizNceEntity;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;

import static com.datech.zjfh.api.common.ivs.CreateSSLClientDefault.createSSLClientDefault;

@Slf4j
public class DelSubscription {

    /**
     * 订阅通知消息
     */
    public static NceAddSubscriptionOutput sendRequest(BizNceEntity nce) {
        CloseableHttpClient httpclient = createSSLClientDefault();
        CloseableHttpResponse response = null;
        Gson gson = new Gson();

        try {
            HttpPost httpPost = new HttpPost("https://" + nce.getIp() + ":26335" + "/restconf/v1/operations/huawei-nce-notification-action:delete-subscription");
            //添加头文件
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Accept-Language", "en-US");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("X-Auth-Token", nce.getSession());
            httpPost.addHeader("roarand", nce.getRoarand());
            // 添加参数信息
            JSONObject params = new JSONObject();
            JSONObject input = new JSONObject();
            params.put("input", input);
            input.put("identifier", nce.getIdentifier());
//            log.info("==========nce params:{}", gson.toJson(params));
//            Header[] headers = httpPost.getAllHeaders();
//            for (int i = 0; i < headers.length; i++) {
//                log.info("==========nce headers:{}", headers[i].toString());
//            }
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
            log.info("==========nce subscription result:{}", sb);
            log.info("==========nce http response code:{}", response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == 200) {
                NceAddSubscriptionResult nceResult = JSONObject.parseObject(sb.toString(), NceAddSubscriptionResult.class);
                return nceResult.getOutput();
            }
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
        return null;
    }

}


