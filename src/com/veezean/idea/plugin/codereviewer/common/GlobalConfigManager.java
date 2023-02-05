package com.veezean.idea.plugin.codereviewer.common;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.veezean.idea.plugin.codereviewer.model.GlobalConfigInfo;
import com.veezean.idea.plugin.codereviewer.model.RecordColumns;
import com.veezean.idea.plugin.codereviewer.util.Logger;

import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 系统配置管理
 *
 * @author Wang Weiren
 * @since 2021/4/26
 */
public final class GlobalConfigManager {
    private static volatile GlobalConfigManager instance;
    private GlobalConfigInfo globalConfigInfo;

    /**
     * 系统默认的字段信息
     */
    private RecordColumns systemDefaultRecordColumns;

    /**
     * 最近一次使用的文件导入导出的目标位置
     */
    private String recentSelectedFileDir;

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
        Logger.info("开始重新加载配置数据操作...");
        this.globalConfigInfo = SerializeUtils.deserialize(".idea_code_review_config", "global_config.dat");
    }

    /**
     * 当前是否为网络版本
     *
     * @return true是网络版，false非网络版
     */
    public VersionType getVersionType() {
        try {
            GlobalConfigInfo globalConfig = getGlobalConfig();
            return VersionType.getVersionType(globalConfig.getVersionType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return VersionType.LOCAL;
    }

    public RecordColumns getSystemDefaultRecordColumns() {
        // 加载预置的值
        if (systemDefaultRecordColumns == null) {
            loadSystemColumnDefine();
        }

        if (systemDefaultRecordColumns == null) {
            throw new CodeReviewException("未获取到配置字段定义数据");
        }

        return systemDefaultRecordColumns;
    }

    private synchronized void loadSystemColumnDefine() {
        if (systemDefaultRecordColumns == null) {
            URL resource = GlobalConfigManager.class.getClassLoader().getResource("SystemColumns.json");
            String json = FileUtil.readString(resource, StandardCharsets.UTF_8.name());
            systemDefaultRecordColumns = JSONUtil.toBean(json, RecordColumns.class);
        }
    }

    public void saveRecentSelectedFileDir(String fileDir) {
        this.recentSelectedFileDir = fileDir;
    }

    public String getRecentSelectedFileDir() {
        return this.recentSelectedFileDir;
    }
}
