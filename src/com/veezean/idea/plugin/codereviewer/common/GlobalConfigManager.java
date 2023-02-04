package com.veezean.idea.plugin.codereviewer.common;

import com.veezean.idea.plugin.codereviewer.model.ColumnValueEnums;
import com.veezean.idea.plugin.codereviewer.model.GlobalConfigInfo;
import com.veezean.idea.plugin.codereviewer.model.RecordColumns;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import com.veezean.idea.plugin.codereviewer.util.RecordColumnBuildFactory;
import org.apache.commons.collections.CollectionUtils;

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
    private ColumnValueEnums columnValueEnums;

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
            systemDefaultRecordColumns = RecordColumnBuildFactory.loadColumnDefines();
        }
    }

    public ColumnValueEnums getColumnValueEnums() {
        // 内存里面已有数据，直接使用
        if (columnValueEnums != null) {
            return columnValueEnums;
        }
        // 读取配置文件中的枚举定义
        ColumnValueEnums valueEnums = RecordColumnBuildFactory.loadColumnValueDefines();

        // 从缓存中读取配置的值
        if (this.columnValueEnums == null || this.columnValueEnums.getValueEnums().isEmpty()) {
            reloadCachedColumnValuesConfig();
        }
        // 缓存中没有值，加载配置文件中预置的值
        if (this.columnValueEnums == null || this.columnValueEnums.getValueEnums().isEmpty()) {
            this.columnValueEnums = valueEnums;
        } else {
            // 缓存中有值，判断下系统预置版本号是否有变大，如果有，则插入新增的字段默认候选值
            if (this.columnValueEnums.getVersion() < valueEnums.getVersion()) {
                valueEnums.getValueEnums().forEach((key, value) -> {
                    // 只将新增字段的信息插入进去，已有字段不变更
                    if (this.columnValueEnums.keyExist(key)) {
                        this.columnValueEnums.putEnum(key, value);
                    }
                });
            }
        }

        if (this.columnValueEnums == null || this.columnValueEnums.getValueEnums().isEmpty()) {
            throw new CodeReviewException("未获取到配置字段候选值数据");
        }

        return this.columnValueEnums;
    }

    /**
     * 保存配置的自定义枚举值数据
     *
     * @param columnValueEnums 配置数据
     */
    public synchronized void saveColumnValuesConfig(ColumnValueEnums columnValueEnums) {
        // TODO 预留，给自定义场景调用
        SerializeUtils.serialize(columnValueEnums, ".idea_code_review_config", "column_values.dat");
    }

    /**
     * 重新加载自定义枚举值数据
     */
    private synchronized void reloadCachedColumnValuesConfig() {
        this.columnValueEnums = SerializeUtils.deserialize(".idea_code_review_config", "column_values.dat");
    }
}
