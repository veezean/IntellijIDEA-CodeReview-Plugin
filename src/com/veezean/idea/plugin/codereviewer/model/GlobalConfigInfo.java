package com.veezean.idea.plugin.codereviewer.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 配置信息
 *
 * @author Wang Weiren
 * @since 2021/4/26
 */
@Data
public class GlobalConfigInfo implements Serializable {
    private static final long serialVersionUID = -1770117176436016022L;
    private boolean netVersion;
    private String serverAddress;
    private String account;
    private String pwd;
}
