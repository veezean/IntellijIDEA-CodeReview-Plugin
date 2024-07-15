package com.veezean.idea.plugin.codereviewer.action.element;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.LanguageTextField;
import com.veezean.idea.plugin.codereviewer.model.Column;

import javax.swing.*;

/**
 * IDEA自带多行文本界面元素构造器
 *
 * @author Veezean
 * @since 2023/2/4
 */
public class EditorTextFieldCreator implements IElementCreator {

    @Override
    public JComponent create(Project project, Column column, boolean editable) {
        EditorTextField editorTextField = new ScrollableCodeField(project);

        return editorTextField;
    }
}
