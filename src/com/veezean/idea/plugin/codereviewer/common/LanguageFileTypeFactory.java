package com.veezean.idea.plugin.codereviewer.common;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.ide.highlighter.JShellFileType;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.jetbrains.jsonSchema.JsonSchemaFileType;
import org.apache.commons.lang.StringUtils;

/**
 * 文件类型工厂
 *
 * @author Wang Weiren
 * @since 2024/6/1
 */
public class LanguageFileTypeFactory {

    /**
     * 根据文件后缀，决定界面渲染的时候使用何种语言的语法
     * @param suffix
     * @return
     */
    public static LanguageFileType getLanguageFileType(String suffix) {
        LanguageFileType fileType = null;
        suffix = StringUtils.isEmpty(suffix) ? "java" : suffix;
        switch (suffix) {
            case "xml":
            case "iml":
                fileType = XmlFileType.INSTANCE;
                break;
            case "html":
                fileType = HtmlFileType.INSTANCE;
                break;
            case "json":
                fileType = JsonFileType.INSTANCE;
                break;
            case "java":
                fileType = JavaFileType.INSTANCE;
                break;
            default:
                fileType = PlainTextFileType.INSTANCE;
        }

        return fileType;
    }
}
