package com.datech.zjfh.api.common.ivs;

//通过阅读本文，您可以了解java调用ivs1800北向登录保活接口的方式和流程。

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static com.datech.zjfh.api.common.ivs.CreateSSLClientDefault.createSSLClientDefault;

/**
 * 调用保活接口
 */

public class KeepAlive {

    /**
     * 通过httpGet方式调用保活接口
     */
    public static String keepAlive(String url, String token) {

        //如果证书无误的情况下可以直接使用下面的代码调用接口，无需绕过证书验证
        //CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpClient httpclient = createSSLClientDefault();
        CloseableHttpResponse response = null;
        String result = "";

        try {
            //创建httpget对象
            HttpGet httpGet = new HttpGet(url + "/common/keepAlive");

            //添加头文件
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Cache-Control", "no-cache");
            httpGet.addHeader("Cookie", token);


            //将请求发送给1800并获取返回结果
            response = httpclient.execute(httpGet);

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

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException {
        // 请求路径
        String url = "https://90.56.29.5:18531/common/keepAlive";
        // 调用login接口获取
        String token = "JSESSIONID=EoL0UJRCkq8Mh3YF8LbOHQS5IAqtanMZ";
        String a = keepAlive(url, token);
    }
}