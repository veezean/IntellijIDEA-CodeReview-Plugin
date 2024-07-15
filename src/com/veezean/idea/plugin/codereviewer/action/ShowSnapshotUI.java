package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.core.thread.ThreadUtil;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.TimerUtil;
import com.veezean.idea.plugin.codereviewer.common.LanguageFileTypeFactory;
import com.veezean.idea.plugin.codereviewer.mark.CodeCommentMarker;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.LanguageUtil;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ShowSnapshotUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel fileInfoLabel;
    private JLabel fileLabel;
    private JLabel snapshotTimeLabel;
    private JLabel snapshotTime;
    private JPanel codePanel;

    private SnapshotEditorField editorTextField;
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 960;

    private ReviewComment commentInfoModel;

    public ShowSnapshotUI(JComponent ideMainWindow, Project project, ReviewComment commentInfoModel) {
        this.commentInfoModel = commentInfoModel;

        // 快照内容填充到编辑器中
        String fileSnapshot = this.commentInfoModel.getFileSnapshot();
        if (StringUtils.isEmpty(fileSnapshot)) {
            fileSnapshot = "No Snapshot Content!!!";
        }
        Document document = EditorFactory.getInstance().createDocument(fileSnapshot);
        LanguageFileType languageFileType = LanguageFileTypeFactory.getLanguageFileType(this.commentInfoModel.fileSuffix());
        editorTextField = new SnapshotEditorField(project, document, languageFileType);

        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        int ideaWidth = ideMainWindow.getWidth();
        int dialogWidth = ideaWidth - 100;
        if (dialogWidth < WIDTH) {
            dialogWidth = WIDTH;
        }
        int ideaHeight = ideMainWindow.getHeight();
        int dialogHeight = ideaHeight - 100;
        if (dialogHeight < HEIGHT) {
            dialogHeight = HEIGHT;
        }
        setLocation(CommonUtil.getWindowRelativePoint(ideMainWindow, dialogWidth, dialogHeight));
        setPreferredSize(new Dimension(dialogWidth, dialogHeight));
        setModal(true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        buttonOK.addActionListener(e -> dispose());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        refreshShowLanguages();
        initSnapshotContent();
    }

    private void initSnapshotContent() {
        String snapshotTime = this.commentInfoModel.getCommitDate();
        String filePath = this.commentInfoModel.getFilePath();

        this.snapshotTime.setText(snapshotTime);
        this.fileInfoLabel.setText(filePath);

        // 初始化内容区域
        this.codePanel.add(editorTextField);

        // 添加标识信息
        // 由于Editor对象必须等到界面渲染出来之后才能获取到，所以此处延迟100ms再去执行
        SwingUtilities.invokeLater(() -> {
            ThreadUtil.safeSleep(100L);
            addCodeMarker();
        });

    }

    private void onOK() {
        // add your code here
        if (editorTextField != null) {
            editorTextField.release();
        }

        dispose();
    }

    private void refreshShowLanguages() {
        this.fileLabel.setText(LanguageUtil.getString("SNAPSHOT_FILE_INFO"));
        this.snapshotTimeLabel.setText(LanguageUtil.getString("SNAPSHOT_TIIME"));
    }

    public static void showSnapshotUI(JComponent mainWindow, Project project, ReviewComment commentInfoModel) {
        ShowSnapshotUI dialog = new ShowSnapshotUI(mainWindow, project, commentInfoModel);

        dialog.pack();
        dialog.setVisible(true);


    }

    public void addCodeMarker() {
        Logger.info("开始添加评审位置标识,threadId=" + Thread.currentThread().getId());
        if (this.editorTextField == null || this.commentInfoModel == null) {
            return;
        }
        Editor myEditorEx = editorTextField.getMyEditorEx();
        if (myEditorEx == null) {
            return;
        }
        CodeCommentMarker.markOneComment(myEditorEx, this.commentInfoModel, true);

        // 跳转到指定的位置
        CaretModel caretModel = myEditorEx.getCaretModel();
        LogicalPosition logicalPosition = caretModel.getLogicalPosition();
        logicalPosition.leanForward(true);
        LogicalPosition logical = new LogicalPosition(commentInfoModel.getStartLine(), logicalPosition.column);
        caretModel.moveToLogicalPosition(logical);
        myEditorEx.getScrollingModel().scrollToCaret(ScrollType.CENTER);
    }
}
