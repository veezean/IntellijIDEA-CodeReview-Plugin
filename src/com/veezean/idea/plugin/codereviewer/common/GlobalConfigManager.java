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
 * @author Veezean, 公众号 @架构悟道
 * @since 2021/4/26
 */
public final class GlobalConfigManager {
    private static volatile GlobalConfigManager instance;
    private GlobalConfigInfo globalConfigInfo;

    /**
     * 系统默认的字段信息
     */
    private RecordColumns systemDefaultRecordColumns;
    private RecordColumns userCustomColumns;

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
            globalConfigInfo = new GlobalConfigInfo();
            Logger.error("没有配置对象信息，初始化一个空的");
        }

        return globalConfigInfo;
    }

    /**
     * 保存配置数据
     *
     */
    public synchronized void saveGlobalConfig() {
        SerializeUtils.serialize(globalConfigInfo, ".idea_code_review_config", "global_config.dat");
    }

    /**
     * 重新加载配置数据
     */
    private synchronized void reloadCachedConfig() {
        Logger.info("开始重新加载配置数据操作...");
        this.globalConfigInfo = SerializeUtils.deserialize(".idea_code_review_config", "global_config.dat");
    }

    public synchronized void resetColumnCaches() {
        this.userCustomColumns = null;
    }

    public synchronized RecordColumns getCustomConfigColumns() {
        // 加载预置的值
        if (userCustomColumns == null || userCustomColumns.getColumns().isEmpty()) {
            loadCustomConfigColumn();
        }

        if (userCustomColumns != null && !userCustomColumns.getColumns().isEmpty()) {
            return userCustomColumns;
        }

        // 没有自定义的，读取系统默认的
        return getSystemDefaultColumns();
    }

    public synchronized RecordColumns getSystemDefaultColumns() {
        // 加载预置的值
        if (systemDefaultRecordColumns == null) {
            loadSystemColumnDefine();
        }

        if (systemDefaultRecordColumns == null) {
            throw new CodeReviewException("未获取到系统配置字段定义数据");
        }

        return systemDefaultRecordColumns;
    }

    /**
     * 保存自定义字段信息
     *
     * @param recordColumns
     */
    public synchronized void saveCustomConfigColumn(RecordColumns recordColumns) {
        SerializeUtils.saveConfigAsJson(recordColumns, ".idea_code_review_config", getCustomConfigFileName());
        this.userCustomColumns = recordColumns;
    }

    private synchronized void loadCustomConfigColumn() {
        this.userCustomColumns = SerializeUtils.readConfigAsJson(RecordColumns.class,
                ".idea_code_review_config", getCustomConfigFileName());
    }

    private String getCustomConfigFileName() {
        if (globalConfigInfo == null || !globalConfigInfo.isNetworkMode()) {
            Logger.info("当前本地模式，尝试加载本地个人定制化配置");
            return "user_custom_columns.json";
        }
        Logger.info("当前网络模式，尝试加载服务端定制化配置");
        return "server_user_custom_columns.json";
    }

    private synchronized void loadSystemColumnDefine() {
        if (systemDefaultRecordColumns == null) {
            URL resource = GlobalConfigManager.class.getClassLoader().getResource("SystemColumns.json");
            String json = FileUtil.readString(resource, StandardCharsets.UTF_8.name());
            systemDefaultRecordColumns = JSONUtil.toBean(json, RecordColumns.class);
        }
    }

    public synchronized void saveRecentSelectedFileDir(String fileDir) {
        this.recentSelectedFileDir = fileDir;
    }

    public synchronized String getRecentSelectedFileDir() {
        return this.recentSelectedFileDir;
    }
}
