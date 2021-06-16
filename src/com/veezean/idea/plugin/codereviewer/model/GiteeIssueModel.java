package com.veezean.idea.plugin.codereviewer.model;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2021/6/12
 */
public class GiteeIssueModel {
    @JSONField(name = "access_token")
    private String accessToken;
    private String repo;
    private String title;
    private String body;
    private String labels;
    @JSONField(name = "security_hole")
    private boolean securityHole;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public boolean isSecurityHole() {
        return securityHole;
    }

    public void setSecurityHole(boolean securityHole) {
        this.securityHole = securityHole;
    }
}
