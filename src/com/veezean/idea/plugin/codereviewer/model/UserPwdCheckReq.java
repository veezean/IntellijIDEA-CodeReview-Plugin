package com.veezean.idea.plugin.codereviewer.model;


/**
 * 客户端鉴权请求对象
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/6/4
 */
public class UserPwdCheckReq {

    private String account;
    /**
     * 密码，MD5加密后的值
     */
    private String password;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
