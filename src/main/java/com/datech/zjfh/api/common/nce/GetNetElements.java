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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;

import static com.datech.zjfh.api.common.ivs.CreateSSLClientDefault.createSSLClientDefault;

@Slf4j
public class GetNetElements {

    /**
     * 查询网元
     */
    public static NceAddSubscriptionOutput sendRequest(BizNceEntity nce) {
        CloseableHttpClient httpclient = createSSLClientDefault();
        CloseableHttpResponse response = null;
        Gson gson = new Gson();

        try {
            HttpGet httpGet = new HttpGet("https://" + nce.getIp() + ":26335" + "/restconf/v2/data/huawei-nce-resource-inventory:network-elements");
            //添加头文件
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Accept-Language", "en-US");
            httpGet.addHeader("Accept", "application/json");
            httpGet.addHeader("X-Auth-Token", nce.getSession());
            response = httpclient.execute(httpGet);
            //获取返回结果并解析
            InputStream content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(content, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            log.info("==========nce 查询网元 result:{}", sb);
            log.info("==========nce 查询网元 response code:{}", response.getStatusLine().getStatusCode());
            Header[] headers = response.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                log.info("==========nce 查询网元 response headers:{}", headers[i].toString());
            }
//            if (response.getStatusLine().getStatusCode() == 200) {
//                NceAddSubscriptionResult nceResult = JSONObject.parseObject(sb.toString(), NceAddSubscriptionResult.class);
//                return nceResult.getOutput();
//            }
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


