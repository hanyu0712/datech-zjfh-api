package com.datech.zjfh.api.common.nce;

//通过阅读本文，您可以了解java调用ivs1800北向登录接口的方式和流程。

import com.alibaba.fastjson.JSONObject;
import com.datech.zjfh.api.common.nce.bean.NceAddSubscriptionResult;
import com.datech.zjfh.api.common.nce.bean.NceLoginResult;
import com.datech.zjfh.api.entity.BizNceEntity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import static com.datech.zjfh.api.common.ivs.CreateSSLClientDefault.createSSLClientDefault;

/**
 * 调用登录接口同时获取token
 */
@Slf4j
public class Login {
    /**
     * 通过httppost方式调用登录接口同时获取token
     */
    public static NceLoginResult loginAndGetToken(BizNceEntity nce) {
        CloseableHttpClient httpclient = createSSLClientDefault();

        //如果证书无误的情况下可以直接使用下面的代码调用接口，无需绕过证书验证
        //CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        HashMap body = new HashMap();
        Gson gson = new Gson();
        String result = "";

        try {
            HttpPut httpPut = new HttpPut("https://" + nce.getIp() + ":26335" + "/rest/plat/smapp/v1/sessions");
            //添加头文件
            httpPut.addHeader("Accept", "application/json");
            httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
            //添加body信息
            body.put("grantType", "password");
            body.put("userName", nce.getAccount());
            body.put("value", nce.getPassword());
            httpPut.setEntity(new StringEntity(gson.toJson(body), "UTF-8"));
            response = httpclient.execute(httpPut);
            log.info("======NceRunner login response code:{}", response.getStatusLine().getStatusCode());
            //获取返回结果并解析
            InputStream content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(content, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
            log.info("nce登录返回结果result:{}", result);
            if (response.getStatusLine().getStatusCode() == 200) {
                nce.setState(1);    //在线
                return JSONObject.parseObject(sb.toString(), NceLoginResult.class);
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
        nce.setState(0);    //不在线
        return null;
    }


}