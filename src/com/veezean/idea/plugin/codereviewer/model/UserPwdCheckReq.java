package com.veezean.idea.plugin.codereviewer.model;


/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
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
