package com.veezean.idea.plugin.codereviewer.consts;

import java.util.Arrays;

/**
 * 版本类型
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/6/11
 */
public enum VersionType {
    LOCAL(0, "单机版本"),
    NETWORK(1, "网络版本（私有服务器）");

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
