package com.veezean.idea.plugin.codereviewer.model;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2024/6/3
 */
public class FileShortInfo {
    public String packageName;
    private String fileName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
