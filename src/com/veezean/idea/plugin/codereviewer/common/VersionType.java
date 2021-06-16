package com.veezean.idea.plugin.codereviewer.common;

import java.util.Arrays;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2021/6/11
 */
public enum VersionType {
    LOCAL(0, "Local"),
    NETWORK(1, "Network(Private Server)"),
    NETWORK_GITEE(2, "Network(Gitee)");

    VersionType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private int value;
    private String desc;

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static VersionType getVersionType(int value) {
        return Arrays.stream(values()).filter(versionType -> versionType.getValue() == value).findFirst().orElse(LOCAL);
    }
}
