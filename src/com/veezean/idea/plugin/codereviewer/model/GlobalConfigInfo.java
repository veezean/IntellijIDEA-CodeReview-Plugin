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
    private int versionType;
    private String serverAddress;
    private String account;
    private String pwd;

    private String giteePrivateToken;
    private String giteeRepoOwner;
    private String giteeRepoPath;

    public int getVersionType() {
        return versionType;
    }

    public void setVersionType(int versionType) {
        this.versionType = versionType;
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

    public String getGiteePrivateToken() {
        return giteePrivateToken;
    }

    public void setGiteePrivateToken(String giteePrivateToken) {
        this.giteePrivateToken = giteePrivateToken;
    }

    public String getGiteeRepoOwner() {
        return giteeRepoOwner;
    }

    public void setGiteeRepoOwner(String giteeRepoOwner) {
        this.giteeRepoOwner = giteeRepoOwner;
    }

    public String getGiteeRepoPath() {
        return giteeRepoPath;
    }

    public void setGiteeRepoPath(String giteeRepoPath) {
        this.giteeRepoPath = giteeRepoPath;
    }
}
