/*
 *
 * Copyright (C) 1999-2016 IFLYTEK Inc.All Rights Reserved.
 *
 * History:
 * Version    Author          Date              Operation
 *   1.0	  onion   2018年6月25日上午10:23:00	      Create
 */

package com.datech.zjfh.api.controller;

import com.datech.zjfh.api.common.bean.ClientVersionBean;
import com.datech.zjfh.api.common.bean.FTPBean;
import com.datech.zjfh.api.common.bean.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 提供版本数据接口
 */
@RestController
@RequestMapping("/sys")
@Slf4j
public class SysVersionController {

    @Autowired
    private FTPBean reFtpBean;
    @Autowired
    private ClientVersionBean clientVersion;

    // 文件上传
    /*@PostMapping("/version/upload")
    public Result<Object> upload(@RequestBody MultipartFile uploadFile) throws IOException {

        // 单文件上传：通过ftp封装的工具类，将文件传递到指定的文件服务器
        if (!uploadFile.isEmpty()) {
            //生成一个新的文件名
            //取原始文件名
            String oldName = uploadFile.getOriginalFilename();
            //生成新文件名
            //UUID.randomUUID();
            String Suffix = oldName.substring(oldName.lastIndexOf("."));
            //保存的文件名
            String newName = UUID.randomUUID()+Suffix;
            // String newName = IDUtils.genImageName();
            // newName = newName + oldName.substring(oldName.lastIndexOf("."));
            //图片上传
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
            String imagePath=simpleDateFormat.format(new Date());

            System.err.println(imagePath);
            System.err.println(newName);


//            boolean result = FtpUtils.uploadFile(FTP_ADDRESS, FTP_PORT, FTP_USERNAME, FTP_PASSWORD,
//                    FTP_BASE_PATH, imagePath, newName, uploadFile.getInputStream());
//            //返回结果
//            if(!result) {
//                resultMap.put("error", 1);
//                resultMap.put("message", "文件上传失败");
//                return resultMap;
//            }
        }

        return Result.OK();
    }*/

    // ftp文件下载
    /*@PostMapping("/version/download")
    @ResponseBody
    public void download(HttpServletResponse response) throws IOException {
        OutputStream out = response.getOutputStream();
//        FtpUtils.downloadFile(out);
        FtpUtils2.readFile(reFtpBean.getRemotePath(), response);
        response.setContentType("application/x-ole-storage;charset=UTF-8"); //这是下载msi格式的。如果需要下载其它的文件，可以去参考一下 常见的MIME类型表
        response.setHeader("Content-disposition", "attachment;filename=" + reFtpBean.getFileName());
    }*/


    @GetMapping(value = "/version/last")
    public Result<Object> getLastVersion() {
        return Result.OK(clientVersion);
    }

}
