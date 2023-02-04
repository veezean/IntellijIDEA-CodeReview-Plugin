package com.veezean.idea.plugin.codereviewer.model;

import java.util.Objects;

/**
 * 字段定义
 *
 * @author Wang Weiren
 * @since 2022/5/21
 */
public class Column {

    /**
     * 字段唯一编码
     */
    private String columnCode;
    /**
     * 对外显示名称
     */
    private String showName;
    /**
     * 排序号
     */
    private int sortIndex;

    /**
     * excel占用列宽
     */
    private int excelColumnWidth;

    /**
     * 是否系统预置，预置字段不允许删除或者更改
     */
    private boolean systemInitialization;
    /**
     * 是否显示在表格中
     */
    private boolean showInTable;
    /**
     * 是否显示在新增界面
     */
    private boolean showInAddPage;
    /**
     * 是否显示在修改界面
     */
    private boolean showInEditPage;
    /**
     * 是否显示在详情界面
     */
    private boolean showInDetailPage;
    /**
     * 此是否可编辑
     */
    private boolean editable;
    /**
     * 确认界面是否允许修改
     */
    private boolean editableInConfirmPage;
    /**
     * 输入类型，单行、多行、下拉框、radio等
     */
    private String inputType;
    /**
     * 是否必填
     */
    private boolean required;

    /**
     * 编辑界面的字段名称
     */
    private String editUiName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return Objects.equals(columnCode, column.columnCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnCode);
    }

    public String getColumnCode() {
        return columnCode;
    }

    public void setColumnCode(String columnCode) {
        this.columnCode = columnCode;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public boolean isSystemInitialization() {
        return systemInitialization;
    }

    public void setSystemInitialization(boolean systemInitialization) {
        this.systemInitialization = systemInitialization;
    }

    public boolean isShowInTable() {
        return showInTable;
    }

    public void setShowInTable(boolean showInTable) {
        this.showInTable = showInTable;
    }

    public boolean isShowInAddPage() {
        return showInAddPage;
    }

    public void setShowInAddPage(boolean showInAddPage) {
        this.showInAddPage = showInAddPage;
    }

    public boolean isShowInEditPage() {
        return showInEditPage;
    }

    public void setShowInEditPage(boolean showInEditPage) {
        this.showInEditPage = showInEditPage;
    }

    public boolean isShowInDetailPage() {
        return showInDetailPage;
    }

    public void setShowInDetailPage(boolean showInDetailPage) {
        this.showInDetailPage = showInDetailPage;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getEditUiName() {
        return editUiName;
    }

    public void setEditUiName(String editUiName) {
        this.editUiName = editUiName;
    }


    public boolean isEditableInConfirmPage() {
        return editableInConfirmPage;
    }

    public void setEditableInConfirmPage(boolean editableInConfirmPage) {
        this.editableInConfirmPage = editableInConfirmPage;
    }

    public int getExcelColumnWidth() {
        return excelColumnWidth;
    }

    public void setExcelColumnWidth(int excelColumnWidth) {
        this.excelColumnWidth = excelColumnWidth;
    }
}
