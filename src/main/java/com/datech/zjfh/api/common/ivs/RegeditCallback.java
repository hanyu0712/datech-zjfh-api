package com.datech.zjfh.api.common.ivs;

import com.google.gson.Gson;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.datech.zjfh.api.common.ivs.CreateSSLClientDefault.createSSLClientDefault;

/**
 * 注册回调信息
 * 接口说明：第三方应用向IVS1800/ITS800注册回调地址信息。
 * 通过该接口第三方应用可以接收到IVS系统返回的回调消息。当传入参数wsUrl中注册
 * 回调地址信息为空字符串时，表示取消接收回调消息。
 */
public class RegeditCallback {
    /*
    /* 通过httppost方式调用接口查询信息
    */
    public static String RegeditCallback(String url, String cookie, Map<String, String> params) {
        CloseableHttpClient httpclient = createSSLClientDefault();
        CloseableHttpResponse response = null;
        Gson gson = new Gson();
        String result = "";
        try {
            HttpPost httpPost = new HttpPost(url);
            //添加头文件
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cache-Control", "no-cache");
            httpPost.addHeader("Cookie", cookie);
            // 添加参数信息
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

    public static void main(String[] args) {
        // 请求路径
        String url = "https://90.85.5.32:18531/users/regeditcallback";
        // 调用login接口获取
        String token = "JSESSIONID=4HmkuMUnGygZGTkqNPSdiHcPLZzIPQAe";
        // 入参
        HashMap params = new HashMap();
        // 回调信息，具体说明如下：
        // 格式：http(s)://{IP地址}:{端口}/应用程序/回调服务模块，例如：https://192.168.1.1:18531/isv/ivscallback
        // 其中{IP地址}、{端口}为Native所在服务器的IP地址和端口号，同时确保能够连通。注册成功后将将向指定url推送消息，调用者需自己拦截回调信息，
        // 拦截到的消息内容参见5.11.3 事件定义类型一个用户对应一个url，多次调用覆盖之前的
        params.put("wsUri", "http://90.253.12.89:7777/loginInfo/regeditcallback");
        // 发送请求
        String result = RegeditCallback(url, token, params);
        System.out.println(result);
    }
}
