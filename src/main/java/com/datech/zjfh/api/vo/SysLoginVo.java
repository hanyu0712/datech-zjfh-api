package com.datech.zjfh.api.vo;

/**
 * 登录信息对象
 */
public class SysLoginVo {
//    @NotBlank(message = "用户名或密码不能为空")
//    @Size(max = 100, message = "密码长度最大为100位字符")
    private String username;
//    @NotBlank(message = "用户名或密码不能为空")
//    @Size(max = 100, message = "密码长度最大为100位字符")
    private String password;
//    @NotBlank(message = "验证码无效")
//    @Length(min = 4, max = 4, message = "验证码长度为4位字符")
    private String captcha;

    private String checkKey;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getCheckKey() {
        return checkKey;
    }

    public void setCheckKey(String checkKey) {
        this.checkKey = checkKey;
    }

}
