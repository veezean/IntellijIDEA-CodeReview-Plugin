package com.veezean.idea.plugin.codereviewer.action.element;

import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.spellchecker.ui.SpellCheckingEditorCustomization;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ErrorStripeEditorCustomization;

import java.awt.*;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2024/5/31
 */
public class ScrollableCodeField extends EditorTextField {

    public ScrollableCodeField(Project project) {
        super("", project, PlainTextFileType.INSTANCE);
        super.setViewerEnabled(false);
        this.setMaximumSize(new Dimension(400, 400));
        this.setMinimumSize(new Dimension(400, 200));
        this.setPreferredSize(new Dimension(400, 200));
    }

    @Override
    protected EditorEx createEditor() {
        EditorEx editor = super.createEditor();
        editor.setVerticalScrollbarVisible(true);
        // 禁用拼写检查
        SpellCheckingEditorCustomization.getInstance(false).customize(editor);
        // 启用横向滚动条
//        HorizontalScrollBarEditorCustomization.ENABLED.customize(editor);
        // 关闭错误语法提示
        ErrorStripeEditorCustomization.DISABLED.customize(editor);
        return editor;
    }
}
