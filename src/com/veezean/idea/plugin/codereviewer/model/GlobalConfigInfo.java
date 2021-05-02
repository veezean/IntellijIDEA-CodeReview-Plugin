package com.veezean.idea.plugin.codereviewer.model;

import java.io.Serializable;

/**
 * 配置信息
 *
 * @author Wang Weiren
 * @since 2021/4/26
 */
public class GlobalConfigInfo implements Serializable {
    private static final long serialVersionUID = -1770117176436016022L;
    private boolean netVersion;
    private String serverAddress;
    private String account;
    private String pwd;

    public boolean isNetVersion() {
        return netVersion;
    }

    public void setNetVersion(boolean netVersion) {
        this.netVersion = netVersion;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
