package com.veezean.idea.plugin.codereviewer.model;

/**
 * <类功能简要描述>
 *
 * @author Veezean
 * @since 2021/6/5
 */
public class UserPwdCheckRespBody {
    private boolean pass;
    private ValuePair userInfo;

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public ValuePair getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(ValuePair userInfo) {
        this.userInfo = userInfo;
    }
}
