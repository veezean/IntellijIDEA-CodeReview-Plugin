package com.veezean.idea.plugin.codereviewer.model;

import com.veezean.idea.plugin.codereviewer.consts.VersionType;

import java.io.Serializable;
import java.util.List;

/**
 * 配置信息
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/4/26
 */
public class GlobalConfigInfo implements Serializable {
    private static final long serialVersionUID = -1770117176436016022L;
    private int versionType;
    private int language;
    private String serverAddress;
    private String account;
    private String pwd;
    // 网络版本，当前选中的项目ID
    private Long selectedServerProjectId;
    private List<ServerProjectShortInfo> cachedProjectList;

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public boolean isNetworkMode() {
        return VersionType.NETWORK.getValue() == versionType;
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

    public Long getSelectedServerProjectId() {
        return selectedServerProjectId;
    }

    public void setSelectedServerProjectId(Long selectedServerProjectId) {
        this.selectedServerProjectId = selectedServerProjectId;
    }

    public List<ServerProjectShortInfo> getCachedProjectList() {
        return cachedProjectList;
    }

    public void setCachedProjectList(List<ServerProjectShortInfo> cachedProjectList) {
        this.cachedProjectList = cachedProjectList;
    }
}
