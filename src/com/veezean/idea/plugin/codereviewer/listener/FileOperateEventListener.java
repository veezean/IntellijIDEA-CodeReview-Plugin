package com.veezean.idea.plugin.codereviewer.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.mark.CodeCommentMarker;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import org.jetbrains.annotations.NotNull;

/**
 * 监听文件窗口的切换事件
 *
 * @author Wang Weiren
 * @since 2024/6/3
 */
public class FileOperateEventListener implements FileEditorManagerListener {
    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        VirtualFile currentFile = event.getNewFile();
        Project project = event.getManager().getProject();

        // 存储当前打开的窗口
        InnerProjectCache projectCache = ProjectLevelService.getService(project).getProjectCache();
        projectCache.recordCurrentOpenedEditFile(currentFile);

        if (currentFile != null) {
            CodeCommentMarker.markOpenedEditor(project, currentFile);
        }
    }
}
