package com.datech.zjfh.api.util;

import com.datech.zjfh.api.common.bean.LoginUser;
import com.datech.zjfh.api.entity.SysLogEntity;
import com.datech.zjfh.api.service.SysLogServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class LogUtil {
    @Resource
    private SysLogServiceImpl sysLogService;

    /*public void addLog(String logContent, Integer logType, Integer operateType) {
        addLog(logContent, logType, operateType, null);
    }*/

    public void addLog(String menu, String logContent, Integer logType, LoginUser user) {
        SysLogEntity sysLog = new SysLogEntity();
        sysLog.setContent(logContent);
        sysLog.setMenu(menu);
        sysLog.setType(logType);
        try {
            //获取request
            HttpServletRequest request = SpringWebContextUtil.getHttpServletRequest();
            //设置IP地址
            sysLog.setDeviceIp(IPUtil.getIpAddr(request));
        } catch (Exception e) {
            sysLog.setDeviceIp("127.0.0.1");
        }
        //获取登录用户信息
//        if(user == null){
//            try {
//                user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//            } catch (Exception e) {
//                //e.printStackTrace();
//            }
//        }
        if(user!=null){
            sysLog.setOpUsername(user.getUsername());
            sysLog.setOpRealname(user.getRealname());
        }
        sysLog.setCreateTime(new Date());
        sysLogService.save(sysLog);
    }

    /*public void addOpLog(LogModel model, LoginUser loginUser, int result){
    	try {
			model.setResult(result);
			addOpLog(model, loginUser);
		} catch (Exception e) {
			
		}
    }
    private void addOpLog(LogModel model, LoginUser user){
    	int opType = model.getOpType();
    	String str = "";
    	if(LogConstant.OPERATE_TYPE_1 ==  opType){
    		str = "查询";
    	} else if (LogConstant.OPERATE_TYPE_2 ==  opType){
    		str = "新增";
    	} else if (LogConstant.OPERATE_TYPE_3 ==  opType){
    		str = "修改";
    	} else if (LogConstant.OPERATE_TYPE_4 ==  opType){
    		str = "删除";
    	} else if (LogConstant.OPERATE_TYPE_5 ==  opType){
    		str = "导入";
    	} else if (LogConstant.OPERATE_TYPE_6 ==  opType){
    		str = "导出";
    	}
    	String resultStr = "";
    	if(model.getResult() == 0){
    		resultStr = "成功";
    	} else if(model.getResult() == 1){
    		resultStr = "失败";
    	}
    	StringBuffer logContent = new StringBuffer();
    	if(StringUtils.isNotBlank(model.getOpModel())){
    		logContent.append(model.getOpModel());
    		logContent.append("-");
    	}
    	logContent.append(model.getOpFun());
    	logContent.append("-");
    	logContent.append(str);
    	logContent.append("-");
    	logContent.append(resultStr);
    	logContent.append(";");
    	if(StringUtils.isNotBlank(model.getText())){
    		logContent.append(model.getText());
    	}
    	addLog(logContent.toString(), LogConstant.LOG_TYPE_2, opType, user);
    }*/
}
