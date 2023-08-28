package com.datech.zjfh.api.config;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.datech.zjfh.api.common.ivs.Ptzcontrol;
import com.datech.zjfh.api.common.nce.AddSubscription;
import com.datech.zjfh.api.common.nce.DelSubscription;
import com.datech.zjfh.api.common.nce.Login;
import com.datech.zjfh.api.common.nce.bean.NceAddSubscriptionOutput;
import com.datech.zjfh.api.common.nce.bean.NceAlarm;
import com.datech.zjfh.api.common.nce.bean.NceLoginResult;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.entity.BizNceEntity;
import com.datech.zjfh.api.entity.BizPresetEntity;
import com.datech.zjfh.api.service.BizCameraServiceImpl;
import com.datech.zjfh.api.service.BizIvsServiceImpl;
import com.datech.zjfh.api.service.BizNceServiceImpl;
import com.datech.zjfh.api.service.BizPresetServiceImpl;
import com.datech.zjfh.api.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@Order(value = 2)
public class NceRunner implements ApplicationRunner {

    @Resource
    private BizNceServiceImpl bizNceService;
    @Resource
    private BizPresetServiceImpl bizPresetService;
    @Resource
    private BizCameraServiceImpl bizCameraService;
    @Resource
    private BizIvsServiceImpl bizIvsService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("======NceRunner start");
        List<BizNceEntity> nceList = bizNceService.list();
        if (CollectionUtils.isNotEmpty(nceList)) {
            ExecutorService service = Executors.newFixedThreadPool(nceList.size());
            for (BizNceEntity nce : nceList) {
                //注销
//            Logout.Logout(url + "/users/logout", tokenObj.toString());
                //登录
                try {
                    NceLoginResult loginResult = Login.loginAndGetToken(nce);
                    log.info("======NceRunner session:{}", loginResult.getAccessSession());
                    nce.setSession(loginResult.getAccessSession());
                    nce.setRoarand(loginResult.getRoaRand());
                    if (nce.getIdentifier() != null) {
                        //删除订阅
                        DelSubscription.sendRequest(nce);
                    }
                    //订阅
                    NceAddSubscriptionOutput output = AddSubscription.sendRequest(nce);
                    if (output != null) {
                        nce.setIdentifier(output.getIdentifier());
                        nce.setSubsEnable(1);//启用订阅
                        service.submit(() -> {
                            sseConnect(output.getUrl(), nce);
                        });
                    } else {
                        log.error("=====NceRunner add subscription error！！！！");
                        nce.setSubsEnable(0);//关闭订阅
                    }
                    bizNceService.updateById(nce);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sseConnect(String url, BizNceEntity nce) {
        log.info("=======nce connect:{} connect start！！！！", nce.getName() + "-" + nce.getIp());
        Client client = ignoreSSLClient();
        WebTarget target = client.target("https://" + nce.getIp() + ":26335" + url);
        EventInput eventInput = target.request().header("X-Auth-Token", nce.getSession()).header("Accept", "application/json").header("Content-Type", "application/json").header("Accept-Language", "en-US").get(EventInput.class);

        while (!eventInput.isClosed()) {
            final InboundEvent inboundEvent = eventInput.read();
            if (inboundEvent == null) {
                // connection has been closed
                break;
            }
            log.info("=====nce connect:{} ,read data:{}", nce.getName() + "-" + nce.getIp(), inboundEvent.getComment() + "; " + inboundEvent.getName() + "; " + inboundEvent.readData(String.class));
            if (StringUtils.isNotBlank(inboundEvent.readData(String.class)) && inboundEvent.readData(String.class).indexOf("riskPointDistance") > 0) {
                NceAlarm nceAlarm = JSONObject.parseObject(inboundEvent.readData(String.class), NceAlarm.class);
                BizPresetEntity preset = bizPresetService.getByDistance(nce.getId(), nceAlarm.getRiskPointDistance());
                log.info("=====nce connect:{} ,告警位置：{}, 对应摄像机预置位:{}", nce.getName() + "-" + nce.getIp(), nceAlarm.getRiskPointDistance() + "米", preset == null ? "空" : preset.getCameraName() + "--" + preset.getPresetName() + "--" + preset.getPresetIndex());
                if (preset != null) {
                    LambdaQueryWrapper<BizCameraEntity> queryWrapper = Wrappers.lambdaQuery();
                    queryWrapper.eq(BizCameraEntity::getDeviceIp, preset.getCameraIp());
                    List<BizCameraEntity> cameraList = bizCameraService.list(queryWrapper);
                    if (CollectionUtils.isNotEmpty(cameraList)) {
                        BizIvsEntity ivs = bizIvsService.getById(cameraList.get(0).getIvsId());
                        if (ivs != null) {
                            String ptzResult = Ptzcontrol.sendRequest("https://" + ivs.getIp() + ":18531", ivs.getToken(), preset.getCameraCode(), preset.getPresetIndex());
                            log.info("nce connect:{} ,调用云台控制结果：{}", nce.getName() + "-" + nce.getIp(), ptzResult);
                        } else {
                            log.error("=====nce connect:{} ,IVS不存在，ivsId:{}",nce.getName() + "-" + nce.getIp(), cameraList.get(0).getIvsId());
                        }
                    } else {
                        log.error("=====nce connect:{} ,预置位摄像头不存在，摄像头IP:{}",nce.getName() + "-" + nce.getIp(), preset.getCameraIp());
                    }
                }
            }
        }
        log.info("=====NceRunner add sse client end！！！！");
    }

    public Client ignoreSSLClient() {
        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new java.security.SecureRandom());
            return ClientBuilder.newBuilder().sslContext(sslcontext).hostnameVerifier((s1, s2) -> true).build();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

}
