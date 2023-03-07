package com.datech.zjfh.api.common.ivs;

import com.alibaba.fastjson.JSONObject;
import com.datech.zjfh.api.common.bean.IvsPtzPresetInfo;
import com.datech.zjfh.api.common.bean.IvsPtzPresetInfoList;
import com.datech.zjfh.api.entity.BizPresetEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.datech.zjfh.api.common.ivs.CreateSSLClientDefault.createSSLClientDefault;

/**
 *  查询设备预置位列表
 */
@Slf4j
public class GetPresetList {
    /**
     * 通过httpget方式查询设备预置位列表
     */
    public static List<BizPresetEntity> getPresetList(String host, String cookie, String cameraCode, String nvrCode) {

        // 请求路径
        String url = host + "/device/ptzpresetlist/"+cameraCode+"/"+nvrCode;

        CloseableHttpClient httpclient = createSSLClientDefault();
        CloseableHttpResponse response = null;
        String result = "";
        try {
            HttpGet httpGet = new HttpGet(url);
            //添加头文件
            httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
            httpGet.addHeader("Cache-Control", "no-cache");
            httpGet.addHeader("Cookie", cookie);

            //将请求发送给1800并获取返回结果
            response = httpclient.execute(httpGet);
            //获取返回结果并解析
            InputStream content = response.getEntity().getContent();
            BufferedReader in = new BufferedReader(new InputStreamReader(content, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
            log.info("查询预置位1800返回结果result:{}", result);
            IvsPtzPresetInfoList presetList = JSONObject.parseObject(result, IvsPtzPresetInfoList.class);
            if (presetList.getPtzPresetInfoList() != null && CollectionUtils.isNotEmpty(presetList.getPtzPresetInfoList().getPtzPresetInfo())) {
                return presetList.getPtzPresetInfoList().getPtzPresetInfo();
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("=========1800 get Preset List error:{}", e.getMessage());
        } finally {
            if (null != response) {
                try {
                    response.close();
                    httpclient.close();
                } catch (IOException e) {
                    log.error("释放连接错误");
                    e.printStackTrace();
                }
            }
        }
        return new ArrayList<>();
    }

    /*public static String buildUrlAndParams(String host) {
        // 请求路径
        String getUrl = host + "/device/deviceList/v1.0";
        // 构建get请求参数，这里只填了必填项
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("deviceType", "2");
        paramsMap.put("fromIndex", "1");
        paramsMap.put("toIndex", "1000");
        getUrl = HttpUtils.buildUrl(getUrl, paramsMap);
        return getUrl;
    }*/

    /*public static void main(String[] args) {
        // 请求路径
        String url = Constants.IVS1800_SERVER_HOST + "/device/deviceList/v1.0";

        // 构建get请求参数，这里只填了必填项
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("deviceType", "2");
        paramsMap.put("fromIndex", "1");
        paramsMap.put("toIndex", "1000");
        url = HttpUtils.buildUrl(url, paramsMap);

        // 调用login接口获取
        String token = "JSESSIONID=EoL0UJRCkq8Mh3YF8LbOHQS5IAqtanMZ";
        // 发送请求
        String result = getSubDeviceList(url, token);
        System.out.println(result);
    }*/
}
