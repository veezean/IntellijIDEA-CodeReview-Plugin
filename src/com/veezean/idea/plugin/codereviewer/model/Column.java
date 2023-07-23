package com.veezean.idea.plugin.codereviewer.model;

import java.util.List;

/**
 * 字段定义
 *
 * @author Veezean
 * @since 2022/5/21
 */
public class Column {
    private Long id;

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
     * 是否显示在IDEA表格中
     */
    private boolean showInIdeaTable;
    /**
     * web端表格中该字段占用宽度
     */
    private int webTableColumnWidth;
    /**
     * 是否显示在WEB端表格中
     */
    private boolean showInWebTable;
    /**
     * 是否显示在新增界面
     */
    private boolean showInAddPage;
    /**
     * 是否显示在编辑界面
     */
    private boolean showInEditPage;
    /**
     * 是否显示在确认界面
     */
    private boolean showInConfirmPage;
    /**
     * 新增时是否可编辑
     */
    private boolean editableInAddPage;
    /**
     * 修改时是否可编辑
     */
    private boolean editableInEditPage;
    /**
     * 确认界面是否可编辑
     */
    private boolean editableInConfirmPage;
    /**
     * 输入类型，单行(TEXT)、多行（TEXTAREA）、下拉框（COMBO_BOX）等
     */
    private String inputType;

    /**
     * 下拉框类型的候选项
     */
    private String dictCollectionCode;

    private List<ValuePair> enumValues;

//    /**
//     * 是否为确认界面的独有字段
//     */
//    private boolean confirmProp;

    /**
     * 是否必填
     */
    private boolean required;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isSupportInExcel() {
        return supportInExcel;
    }

    public void setSupportInExcel(boolean supportInExcel) {
        this.supportInExcel = supportInExcel;
    }

    public int getExcelColumnWidth() {
        return excelColumnWidth;
    }

    public void setExcelColumnWidth(int excelColumnWidth) {
        this.excelColumnWidth = excelColumnWidth;
    }

    public boolean isSystemInitialization() {
        return systemInitialization;
    }

    public void setSystemInitialization(boolean systemInitialization) {
        this.systemInitialization = systemInitialization;
    }

    public boolean isShowInIdeaTable() {
        return showInIdeaTable;
    }

    public void setShowInIdeaTable(boolean showInIdeaTable) {
        this.showInIdeaTable = showInIdeaTable;
    }

    public int getWebTableColumnWidth() {
        return webTableColumnWidth;
    }

    public void setWebTableColumnWidth(int webTableColumnWidth) {
        this.webTableColumnWidth = webTableColumnWidth;
    }

    public boolean isShowInWebTable() {
        return showInWebTable;
    }

    public void setShowInWebTable(boolean showInWebTable) {
        this.showInWebTable = showInWebTable;
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

    public boolean isShowInConfirmPage() {
        return showInConfirmPage;
    }

    public void setShowInConfirmPage(boolean showInConfirmPage) {
        this.showInConfirmPage = showInConfirmPage;
    }

    public boolean isEditableInAddPage() {
        return editableInAddPage;
    }

    public void setEditableInAddPage(boolean editableInAddPage) {
        this.editableInAddPage = editableInAddPage;
    }

    public boolean isEditableInEditPage() {
        return editableInEditPage;
    }

    public void setEditableInEditPage(boolean editableInEditPage) {
        this.editableInEditPage = editableInEditPage;
    }

    public boolean isEditableInConfirmPage() {
        return editableInConfirmPage;
    }

    public void setEditableInConfirmPage(boolean editableInConfirmPage) {
        this.editableInConfirmPage = editableInConfirmPage;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public String getDictCollectionCode() {
        return dictCollectionCode;
    }

    public void setDictCollectionCode(String dictCollectionCode) {
        this.dictCollectionCode = dictCollectionCode;
    }

    public List<ValuePair> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<ValuePair> enumValues) {
        this.enumValues = enumValues;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
