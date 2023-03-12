package com.veezean.idea.plugin.codereviewer.model;

import java.util.List;
import java.util.Objects;

/**
 * 字段定义
 *
 * @author Veezean, 公众号 @架构悟道
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
     * 是否支持导出到表格中
     */
    private boolean supportInExcel;

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
     * 是否显示在确认界面
     */
    private boolean showInComfirmPage;
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
     * 下拉框类型的候选项（服务端查询key，优先级高于enumValues）
     */
    private String serverComboBoxCode;

    /**
     * 下拉框类型的候选项
     */
    private List<String> enumValues;

    /**
     * 是否为确认界面的独有字段
     */
    private boolean confirmProp;

    /**
     * 是否必填
     */
    private boolean required;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return Objects.equals(columnCode, column.columnCode);
    }

    public boolean isSupportInExcel() {
        return supportInExcel;
    }

    public void setSupportInExcel(boolean supportInExcel) {
        this.supportInExcel = supportInExcel;
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

    public boolean isShowInComfirmPage() {
        return showInComfirmPage;
    }

    public void setShowInComfirmPage(boolean showInComfirmPage) {
        this.showInComfirmPage = showInComfirmPage;
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

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    public boolean isConfirmProp() {
        return confirmProp;
    }

    public void setConfirmProp(boolean confirmProp) {
        this.confirmProp = confirmProp;
    }

    public String getServerComboBoxCode() {
        return serverComboBoxCode;
    }

    public void setServerComboBoxCode(String serverComboBoxCode) {
        this.serverComboBoxCode = serverComboBoxCode;
    }
}
