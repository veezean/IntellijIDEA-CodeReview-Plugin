package com.veezean.idea.plugin.codereviewer.common;

import com.veezean.idea.plugin.codereviewer.model.GlobalConfigInfo;

/**
 * 系统配置管理
 *
 * @author Wang Weiren
 * @since 2021/4/26
 */
public final class GlobalConfigManager {
    private static volatile GlobalConfigManager instance;
    private GlobalConfigInfo globalConfigInfo;
    private GlobalConfigManager() {

    }

    /**
     * 获取单例对象
     *
     * @return instance
     */
    public static synchronized GlobalConfigManager getInstance() {
        if (instance == null) {
            instance = new GlobalConfigManager();
        }
        return instance;
    }

    /**
     * 读取配置数据
     *
     * @return 配置数据
     */
    public GlobalConfigInfo getGlobalConfig() {
        if (globalConfigInfo == null) {
            reloadCachedConfig();
        }
        if (globalConfigInfo == null) {
            throw new CodeReviewException("未获取到配置数据");
        }

        return globalConfigInfo;
    }

    /**
     * 保存配置数据
     *
     * @param globalConfigInfo 配置数据
     */
    public synchronized void saveGlobalConfig(GlobalConfigInfo globalConfigInfo) {
        SerializeUtils.serialize(globalConfigInfo, ".idea_code_review_config", "global_config.dat");
        this.globalConfigInfo = globalConfigInfo;
    }

    /**
     * 重新加载配置数据
     */
    private synchronized void reloadCachedConfig() {
        this.globalConfigInfo = SerializeUtils.deserialize(".idea_code_review_config", "global_config.dat");
    }

    /**
     * 当前是否为网络版本
     *
     * @return true是网络版，false非网络版
     */
    public boolean isNetVersion() {
        try {
            GlobalConfigInfo globalConfig = getGlobalConfig();
            return globalConfig.isNetVersion();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
