package com.veezean.idea.plugin.codereviewer.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.veezean.idea.plugin.codereviewer.model.ColumnValueEnums;
import com.veezean.idea.plugin.codereviewer.model.RecordColumns;

import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 记录的字段定义构建工厂方法
 *
 * @author Wang Weiren
 * @since 2022/5/21
 */
public class RecordColumnBuildFactory {

    /**
     * 字段信息加载
     *
     * @return
     */
    public static RecordColumns loadColumnDefines() {
        URL resource = RecordColumnBuildFactory.class.getClassLoader().getResource("SystemColumns.json");
        String json = FileUtil.readString(resource, StandardCharsets.UTF_8.name());
        RecordColumns columns = JSONUtil.toBean(json, RecordColumns.class);
        return columns;
    }

    /**
     * 字段取值枚举定义加载
     *
     * @return
     */
    public static ColumnValueEnums loadColumnValueDefines() {
        URL resource = RecordColumnBuildFactory.class.getClassLoader().getResource("ColumnValueEnums.json");
        String json = FileUtil.readString(resource, StandardCharsets.UTF_8.name());
        ColumnValueEnums columns = JSONUtil.toBean(json, ColumnValueEnums.class);
        return columns;
    }
}
