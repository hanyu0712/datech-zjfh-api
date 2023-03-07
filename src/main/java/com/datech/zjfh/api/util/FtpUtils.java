package com.datech.zjfh.api.util;

import com.datech.zjfh.api.common.bean.FTPBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;

@Slf4j
//@Component
public class FtpUtils {

    @Autowired
    private FTPBean reFtpBean;

    private static FTPBean ftpBean;

    @PostConstruct
    public  void init() {
        ftpBean = reFtpBean;
        log.info("初始化完成");
    }

    // 连接ftp
    public static FTPClient getConnection() {
        FTPClient ftpClient = new FTPClient();
        try {
            // 设置连接机器
            ftpClient.connect(ftpBean.getHostname(), ftpBean.getPort());
            ftpClient.login(ftpBean.getUsername(), ftpBean.getPassword());
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                log.info("ftp连接失败");
                ftpClient.disconnect(); // 断开连接
                return null;
            } else {
                log.info("ftp连接成功");
            }
            ftpClient.setControlEncoding("UTF-8");// 设置字符编码
            // 将文件类型设置成二进制
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 创建要存储文件夹的目录: 主要目录只能一级一级创建，不能一次创建多层; 在 选择创建目录是一定要看是否有写权限，不然失败
//            ftpClient.makeDirectory(ftpBean.getBasePath());
            // 改变默认存放的位置
//            ftpClient.changeWorkingDirectory(ftpBean.getRemotePath());
            //开启被动模式，否则文件上传不成功，也不报错
//            ftpClient.enterLocalPassiveMode();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }
        return ftpClient;
    }


    // 上传文件
    public static boolean uploadFile(String basePath,
                                     String filePath, String filename, InputStream input) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        ftp.enterLocalPassiveMode();
        try {
            int reply;
            ftp.connect(ftpBean.getHostname(), ftpBean.getPort());// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(ftpBean.getUsername(), ftpBean.getPassword());// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return result;
            }
            //切换到上传目录
            if (!ftp.changeWorkingDirectory(basePath+filePath)) {
                //如果目录不存在创建目录
                String[] dirs = filePath.split("/");
                String tempPath = basePath;
                for (String dir : dirs) {
                    if (null == dir || "".equals(dir)) continue;
                    tempPath += "\\" + dir;
                    if (!ftp.changeWorkingDirectory(tempPath)) {
                        if (!ftp.makeDirectory(tempPath)) {
                            return result;
                        } else {
                            ftp.changeWorkingDirectory(tempPath);
                        }
                    }
                }
            }
            //设置上传文件的类型为二进制类型
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            //上传文件
            if (!ftp.storeFile(filename, input)) {

                System.err.println(ftp.storeFile(filename, input));

                return result;
            }
            input.close();
            ftp.logout();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return result;
    }


    /**
     * 文件下载
     */
    public static void downloadFile(OutputStream out) {
        FTPClient ftpClient = getConnection();
        if (ftpClient == null) {
            return;
        }
        try {
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalActiveMode();
            ftpClient.changeWorkingDirectory(ftpBean.getRemotePath());
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile ftpFile : ftpFiles) {
                if (ftpBean.getFileName().equals(ftpFile.getName())) {
                    boolean result = ftpClient.retrieveFile(ftpFile.getName(), out);
                    log.info("ftp下载结果：" + result);
                }
            }
            ftpClient.logout();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
