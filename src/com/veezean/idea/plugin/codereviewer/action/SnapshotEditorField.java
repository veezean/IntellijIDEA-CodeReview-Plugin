package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.spellchecker.ui.SpellCheckingEditorCustomization;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ErrorStripeEditorCustomization;
import com.intellij.ui.HorizontalScrollBarEditorCustomization;
import com.veezean.idea.plugin.codereviewer.util.Logger;

import java.awt.*;

/**
 * 快照界面使用的定制化编辑器
 *
 * @author Veezean
 * @since 2024/5/31
 */
public class SnapshotEditorField extends EditorTextField {

    private EditorEx myEditorEx;

    public SnapshotEditorField(Project project, Document document, LanguageFileType languageFileType) {
        super(document, project, languageFileType);
        super.setViewerEnabled(false);
        super.setOneLineMode(false);

        Logger.info("SnapshotEditorField初始化构建完成,threadId=" + Thread.currentThread().getId());
    }

    @Override
    protected EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        editor.setVerticalScrollbarVisible(true);
        editor.setHorizontalScrollbarVisible(true);

        // 禁用拼写检查
        SpellCheckingEditorCustomization.getInstance(false).customize(editor);
        // 启用横向滚动条
        HorizontalScrollBarEditorCustomization.ENABLED.customize(editor);
        // 关闭错误语法提示
        ErrorStripeEditorCustomization.DISABLED.customize(editor);

        this.myEditorEx = editor;
        Logger.info("myEditorEx自定义初始化完成,threadId=" + Thread.currentThread().getId());

        return editor;
    }

    public Editor getMyEditorEx() {
        return this.myEditorEx;
    }

    public void release() {
        if (myEditorEx != null) {
            myEditorEx = null;
        }
    }
}
