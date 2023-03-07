package com.datech.zjfh.api.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.datech.zjfh.api.common.PageService;
import com.datech.zjfh.api.common.bean.IvsAddIntelligentDataResult;
import com.datech.zjfh.api.common.ivs.AddIntelligentData;
import com.datech.zjfh.api.common.ivs.DeleteIntelligentData;
import com.datech.zjfh.api.common.ivs.GetSubDeviceList;
import com.datech.zjfh.api.entity.BizCameraEntity;
import com.datech.zjfh.api.entity.BizIvsEntity;
import com.datech.zjfh.api.mapper.BizCameraMapper;
import com.datech.zjfh.api.util.LogUtil;
import com.datech.zjfh.api.util.RedisUtil;
import com.datech.zjfh.api.vo.BizCameraCodeVo;
import com.datech.zjfh.api.vo.SysOrgTreeVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BizCameraServiceImpl extends ServiceImpl<BizCameraMapper, BizCameraEntity> implements PageService<BizCameraEntity> {
    @Value("${alarmServer.receiveAddr}")
    public String receiveAddr;
    @Resource
    private LogUtil logUtil;
    @Resource
    private RedisUtil redisUtil;
    @Autowired
    private static RestTemplate restTemplate;
    @Resource
    private BizCameraMapper bizCameraMapper;

    public void addArea(SysOrgTreeVo node, List<Integer> ivsIdList) {
        if (node.getLevel() == 4) {
            QueryWrapper<BizCameraEntity> query = new QueryWrapper<>();
            query.select(" DISTINCT area ");
            query.eq("org_id", node.getId());
            query.in("ivs_id", ivsIdList);
            List<BizCameraEntity> cameraList = this.list(query);
            List<SysOrgTreeVo> child = new ArrayList<>();
            cameraList.forEach(area -> {
                //生成第5级节点
                SysOrgTreeVo areaNode = new SysOrgTreeVo();
                areaNode.setName(area.getArea());
                areaNode.setLevel(5);
                areaNode.setPid(node.getId());
                child.add(areaNode);
                //生成第6级摄像头信息
                LambdaQueryWrapper<BizCameraEntity> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.eq(BizCameraEntity::getOrgId, node.getId());
                queryWrapper.eq(BizCameraEntity::getArea, area.getArea());
                queryWrapper.in(BizCameraEntity::getIvsId, ivsIdList);
                List<BizCameraEntity> cameraEntityList = this.list(queryWrapper);
                if (cameraEntityList != null) {
                    List<BizCameraCodeVo> codeVoList = new ArrayList<>();
                    cameraEntityList.forEach(camera -> {
                        BizCameraCodeVo codeVo = new BizCameraCodeVo();
                        codeVo.setCode(camera.getCode());
                        codeVo.setName(camera.getName());
                        codeVo.setIp(camera.getDeviceIp());
                        codeVoList.add(codeVo);
                    });
                    areaNode.setCameraList(codeVoList);
                }

            });
            node.setChildList(child);
        } else {
            node.getChildList().forEach(a -> addArea(a, ivsIdList));
        }
    }

    /**
     * 同步ivs1800摄像头数据
     */
    public void syncIvs1800Camera(BizIvsEntity ivs) {
        // 查询IVS摄像头
        List<BizCameraEntity> ivsCameraList = GetSubDeviceList.getSubDeviceList("https://" + ivs.getIp() + ":18531", ivs.getToken());
        if (CollectionUtils.isNotEmpty(ivsCameraList)) {
            List<BizCameraEntity> cameraEntityList = this.list();
            //第一次同步摄像头
            boolean firstSync = cameraEntityList.size() == 0;
            if (firstSync) {
                ivsCameraList.forEach(o -> {
                    //订阅告警(离线摄像头也可以订阅告警)
                    addIntelligentData(o, ivs);
                    o.setCreateTime(new Date());
                    o.setIvsId(ivs.getId());
                });
                bizCameraMapper.saveBatchXml(ivsCameraList);
            } else {
                Map<String, BizCameraEntity> entityMap = cameraEntityList.stream().collect(Collectors.toMap(BizCameraEntity::getCode, Function.identity()));
                for (BizCameraEntity ivsCamera : ivsCameraList) {
                    LambdaUpdateWrapper<BizCameraEntity> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(BizCameraEntity::getCode, ivsCamera.getCode());
                    //同步离线摄像头
                    if (ivsCamera.getStatus() == 0) {
                        if (entityMap.get(ivsCamera.getCode()) == null) {
                            //数据不存在，则新增，订阅告警(离线摄像头也可以订阅告警)
                            addIntelligentData(ivsCamera, ivs);
//                            o.setSubsEnable(1);
                            ivsCamera.setCreateTime(new Date());
                            ivsCamera.setIvsId(ivs.getId());
                            bizCameraMapper.insert(ivsCamera);
                        } else {
                            //数据已存在
                            BizCameraEntity entityUpdate = new BizCameraEntity();
                            //更新离线
                            entityUpdate.setStatus(0);
                            if (entityMap.get(ivsCamera.getCode()).getSubsEnable() == 1) {
                                //订阅告警(离线摄像头也可以订阅告警)
                                entityUpdate.setCode(ivsCamera.getCode());//设置code是订阅告警使用
                                addIntelligentData(entityUpdate, ivs);
                            }
                            bizCameraMapper.update(entityUpdate, updateWrapper);
                        }
                    } else {
                        //同步在线、休眠摄像头
                        if (entityMap.get(ivsCamera.getCode()) == null) {
                            //数据不存在，则新增，订阅告警
                            addIntelligentData(ivsCamera, ivs);
                            ivsCamera.setCreateTime(new Date());
                            ivsCamera.setIvsId(ivs.getId());
                            bizCameraMapper.insert(ivsCamera);
                        } else {
                            //数据已存在
                            BizCameraEntity entityUpdate = null;
                            //如果本地数据为离线，更新在线、休眠状态，订阅告警
                            if (entityMap.get(ivsCamera.getCode()).getStatus() == 0) {
                                entityUpdate = new BizCameraEntity();
                                entityUpdate.setStatus(ivsCamera.getStatus());
                            }
                            if (entityMap.get(ivsCamera.getCode()).getSubsEnable() == 1) {
                                if (entityUpdate == null) {
                                    entityUpdate = new BizCameraEntity();
                                }
                                //订阅告警
                                entityUpdate.setCode(ivsCamera.getCode());//设置code是订阅告警使用
                                addIntelligentData(entityUpdate, ivs);
                            }
                            if (entityUpdate != null) {
                                bizCameraMapper.update(entityUpdate, updateWrapper);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 订阅告警
     *
     * @return 订阅ID
     */
    public int addIntelligentData(BizCameraEntity camera, BizIvsEntity ivs) {
//        HashMap params = new HashMap();
//        HashMap subscribeListObject = new HashMap();
//        List subscribeObject = new ArrayList();
//        HashMap body = new HashMap();
//        // 订阅类别, 可同时带多个类别(必填)
//        params.put("SubscribeDetail", "0");
//
//        // 告警信息接收地址，只支持HTTPS URL, 只支持IP，不支持域名。取值范围：数字或字符，字节数不超过512(必填)
//        params.put("ReceiveAddr", receiveAddr);
//        // 设备编码类型(默认为0)：0-平台编码 1-互联编码。(非必填)
//        params.put("CodeType", 0);
//        // 回调结果是否需要图片（base64编码）。0-不要图(默认) 1-要全景图 2-要抠图或特写图 3-要全景图和抠图或特写图
//        params.put("ResultImgType", 3);
//
//        subscribeObject.add(params);
//        subscribeListObject.put("SubscribeObject", subscribeObject);
//        //将完整请求参数放入body
//        body.put("SubscribeListObject", subscribeListObject);
//        // 订阅资源路径。支持批量和单个订阅(必填)
//        params.put("ResourceURI", camera.getCode());
        String addResult = AddIntelligentData.sendRequest(restTemplate, "https://" + ivs.getIp() + ":18531", ivs.getToken(), camera.getCode(), receiveAddr);
        log.info("订阅结果：{}", addResult);
        IvsAddIntelligentDataResult ivsResultList = JSONObject.parseObject(addResult, IvsAddIntelligentDataResult.class);
        if (ivsResultList.getResultCode() == 0 && ivsResultList.resultInfoList.get(0).getResult() == 0) {
            camera.setSubscribeId(ivsResultList.resultInfoList.get(0).getSubscribeID());
            camera.setSubsEnable(1);
            return 0;
        } else {
            if (ivsResultList.getResultCode() != 0) {
                log.error("------摄像头[{}]订阅告警失败：{}, {}", camera.getCode(), ivsResultList.getResultCode(), "null");
                return ivsResultList.getResultCode();
            } else {
                log.error("------摄像头[{}]订阅告警失败：{}, {}", camera.getCode(), ivsResultList.getResultCode(), ivsResultList.resultInfoList.get(0).getResult());
                return ivsResultList.resultInfoList.get(0).getResult();
            }
        }
    }


    /**
     * 删除订阅告警
     *
     * @param subscribeId
     * @return 订阅ID
     */
    public int deleteIntelligentData(String subscribeId, BizIvsEntity ivs) {
//        HashMap<String, String> params = new HashMap<>();
//        params.put("SubscribeID", subscribeId);
//        List<HashMap<String, String>> subscribeObject = new ArrayList<>();
//        subscribeObject.add(params);
//        //将完整请求参数放入body
//        HashMap<String, List<HashMap<String, String>>> body = new HashMap<>();
//        body.put("SubscribeIDList", subscribeObject);
//        log.info("=========" + JSONObject.toJSONString(body));
        String delResult = DeleteIntelligentData.deleteIntelligentData("https://" + ivs.getIp() + ":18531", ivs.getToken(), subscribeId);
        log.info("删除订阅结果：{}", delResult);
        IvsAddIntelligentDataResult ivsResultList = JSONObject.parseObject(delResult, IvsAddIntelligentDataResult.class);
        if (ivsResultList.getResultCode() == 0 && ivsResultList.resultInfoList.get(0).getResult() == 0) {
            return 0;
        } else {
            if (ivsResultList.getResultCode() != 0) {
                log.error("------摄像头[{}]删除订阅告警失败：{}, {}", subscribeId, ivsResultList.getResultCode(), "null");
                return ivsResultList.getResultCode();
            } else {
                log.error("------摄像头[{}]删除订阅告警失败：{}, {}", subscribeId, ivsResultList.getResultCode(), ivsResultList.resultInfoList.get(0).getResult());
                return ivsResultList.resultInfoList.get(0).getResult();
            }
        }
    }



}
