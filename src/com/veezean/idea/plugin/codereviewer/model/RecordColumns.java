package com.veezean.idea.plugin.codereviewer.model;

import com.veezean.idea.plugin.codereviewer.common.CodeReviewException;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表格中对应字段信息定义
 *
 * @author Veezean, 公众号 @架构悟道
 * @since 2022/5/21
 */
public class RecordColumns {

    private List<Column> columns = new ArrayList<>();

    public Column getColumnByIndex(int index) {
        int size = columns.size();
        if (index < size) {
            return columns.get(index);
        }
        return null;
    }

    public void addColumn(Column col) {
        if (col == null) {
            throw new CodeReviewException("列定义不能为空");
        }
        if (columns.contains(col)) {
            throw new CodeReviewException("已存在相同code的字段定义");
        }
        columns.add(col);
    }

    public void addColumns(List<Column> cols) {
        if (cols == null || cols.isEmpty()) {
            throw new CodeReviewException("列定义不能为空");
        }
        columns.addAll(cols);
    }

    public Object[] getTableHeaderColumns() {
        return getTableAvailableColumns().stream().map(Column::getShowName).toArray();
    }

    public List<Column> getTableAvailableColumns() {
        List<Column> columns = this.columns.stream()
                .filter(Column::isShowInIdeaTable)
                .sorted(Comparator.comparingInt(Column::getSortIndex))
                .collect(Collectors.toList());
        if (columns.isEmpty()) {
            throw new CodeReviewException("表格中列字段为空，至少需要保证有1个字段！");
        }
        return columns;
    }

    public List<Column> getExcelColumns() {
        List<Column> columns = this.columns.stream()
                .filter(Column::isSupportInExcel)
                .sorted(Comparator.comparingInt(Column::getSortIndex))
                .collect(Collectors.toList());
        if (columns.isEmpty()) {
            throw new CodeReviewException("表格中列字段为空，至少需要保证有1个字段！");
        }
        return columns;
    }

    public Optional<Column> getColumnByShowName(String showName) {
        return this.columns.stream()
                .filter(column -> StringUtils.equals(column.getShowName(), showName))
                .findFirst();
    }

    public Optional<Column> getColumnByCode(String colCode) {
        return this.columns.stream()
                .filter(column -> StringUtils.equals(column.getColumnCode(), colCode))
                .findFirst();
    }


    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}
