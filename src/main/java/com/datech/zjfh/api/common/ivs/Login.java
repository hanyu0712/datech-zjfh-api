package com.datech.zjfh.api.common.ivs;

//通过阅读本文，您可以了解java调用ivs1800北向登录接口的方式和流程。

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
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

public class Login {
    /**
     * 通过httppost方式调用登录接口同时获取token
     */
    public static String loginAndGetToken(String url, String userName, String password) {
        CloseableHttpClient httpclient = createSSLClientDefault();

        //如果证书无误的情况下可以直接使用下面的代码调用接口，无需绕过证书验证
        //CloseableHttpClient httpclient = HttpClients.createDefault();
        String jsessionId[] = null;
        String token = null;
        CloseableHttpResponse response = null;
        HashMap body = new HashMap();
        Gson gson = new Gson();
        String result = "";
        String resultCode;

        try {
            //创建httppost对象
            HttpPost httpPost = new HttpPost(url + "/loginInfo/login/v1.0");

            //添加头文件
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cache-Control", "no-cache");

            //添加body信息
            body.put("userName", userName);
            body.put("password", password);
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
            System.out.println(result);
            //创建一个JsonParser
            JsonParser parser = new JsonParser();
            //通过JsonParser对象可以把json格式的字符串解析成一个JsonElement对象
            JsonObject el = (JsonObject) parser.parse(result);
            resultCode = el.get("resultCode").toString();
            if (resultCode.equals("0")) {
                //从返回结果头文件中获取token
                Header[] headers = response.getHeaders("Set-Cookie");
                jsessionId = headers[0].getValue().split(";");
                token = jsessionId[0];
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
        return token;
    }


    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException {
        //请求路径
        String url = "https://127.0.0.1:9026/ivs/callback";
//        String url = "https://127.0.0.1:8082/loginInfo/login/v1.0";
        //用户名（必填）
        String userName = "admin";
        //密码（必填）
        String password = "super123";

        String a = loginAndGetToken(url, userName, password);
        System.out.println(a);
    }
}