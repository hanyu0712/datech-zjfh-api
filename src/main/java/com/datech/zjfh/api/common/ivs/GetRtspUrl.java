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
 * 获取视频URL信息
 */
public class GetRtspUrl {
    /**
     * 通过httppost方式调用接口获取视频URL信息
     */
    public static String getRtspUrl(String url, String Cookie, Map<String, String> params) {
        CloseableHttpClient httpclient = createSSLClientDefault();
        CloseableHttpResponse response = null;
        Gson gson = new Gson();
        String result = "";
        try {
            HttpPost httpPost = new HttpPost(url);
            //添加头文件
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Cache-Control", "no-cache");
            httpPost.addHeader("Cookie", Cookie);
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
        String url = "https://90.56.29.5:18531/video/rtspurl/v1.0";
        // 调用login接口获取
        String token = "JSESSIONID=v8iSX3AhIzmLGIdCrzvyqh4EqJtWGYPB";
        // 单个名单库
        HashMap params = new HashMap();
        HashMap mediaURLParam = new HashMap();
        // 组ID，从1开始的 long型
        mediaURLParam.put("broadCastType", 0);
        //协议类型（默认为1）：1：UDP 2：TCP 说明：推荐使用TCP
        mediaURLParam.put("protocolType", 1);
        // 目前支持业务类型：1：实时浏览3：录像下载4：录像回放5：语音对讲6: 语音广播
        mediaURLParam.put("serviceType", 1);
        // 码流类型（默认为 1）：1：主码流 2：辅码流1 3：辅码流2
        mediaURLParam.put("streamType", 1);
        //是否直连优先（默认为 0）：0：否 1：是
        mediaURLParam.put("transMode", 0);
        //（可填参数）：● 0：IVSCU或eSDK● 1：标准RTSP客户端
        mediaURLParam.put("clientType", 1);
        // 实时浏览或录像的摄像机编码（获取子设备列表返回值cameraBriefInfoscode字段）
        params.put("cameraCode", "00057524295268260101");
        //RTSPURL请求参数
        params.put("mediaURLParam",mediaURLParam);
        // 发送请求
        String result = getRtspUrl(url, token, params);
        System.out.println(result);
    }
}
