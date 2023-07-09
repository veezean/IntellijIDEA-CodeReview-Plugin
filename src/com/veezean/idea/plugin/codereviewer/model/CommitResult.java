package com.veezean.idea.plugin.codereviewer.model;

import java.util.List;
import java.util.Map;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2023/6/30
 */
public class CommitResult {
    private boolean success;
    private String errDesc;
    private Map<String, Long> versionMap;
    private List<String> failedIds;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrDesc() {
        return errDesc;
    }

    public void setErrDesc(String errDesc) {
        this.errDesc = errDesc;
    }

    public Map<String, Long> getVersionMap() {
        return versionMap;
    }

    public void setVersionMap(Map<String, Long> versionMap) {
        this.versionMap = versionMap;
    }

    public List<String> getFailedIds() {
        return failedIds;
    }

    public void setFailedIds(List<String> failedIds) {
        this.failedIds = failedIds;
    }
}
