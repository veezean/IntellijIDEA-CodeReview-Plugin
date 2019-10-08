package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.veezean.idea.plugin.codereviewer.common.DataPersistentUtil;
import com.veezean.idea.plugin.codereviewer.common.DateTimeUtil;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.common.ProjectInstanceManager;
import com.veezean.idea.plugin.codereviewer.model.ReviewCommentInfoModel;

public class AddNewComment extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent e) {

        //获取当前操作的类文件
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        //获取当前类文件的路径
        String classPath = psiFile.getVirtualFile().getName();

        Editor data = e.getData(CommonDataKeys.EDITOR);

        SelectionModel selectionModel = data.getSelectionModel();
        // 获取当前选择的内容
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null || "".equals(selectedText)) {
            return;
        }

        Document document = data.getDocument();
        int startLine = document.getLineNumber(selectionModel.getSelectionStart()) + 1;
        int endLine = document.getLineNumber(selectionModel.getSelectionEnd()) + 1;


        ReviewCommentInfoModel model = new ReviewCommentInfoModel();
        model.setComments("");
        model.setStartLine(startLine);
        model.setEndLine(endLine);
        model.setContent(selectedText);
        model.setFilePath(classPath);
        long currentTimeMillis = System.currentTimeMillis();
        model.setIdentifier(currentTimeMillis);
        model.setDateTime(DateTimeUtil.time2String(currentTimeMillis));

        Project project = e.getProject();
        String locationHash = project.getLocationHash();
        InnerProjectCache projectCache = ProjectInstanceManager.getInstance().getProjectCache(locationHash);
        if (projectCache == null) {
            projectCache = new InnerProjectCache(project);
            ProjectInstanceManager.getInstance().addProjectCache(locationHash, projectCache);
        }

        ReviewCommentInfoModel lastCommentModel = projectCache.getLastCommentModel();
        if (lastCommentModel != null) {
            model.setReviewer(lastCommentModel.getReviewer());
            model.setType(lastCommentModel.getType());
            model.setSeverity(lastCommentModel.getSeverity());
            model.setFactor(lastCommentModel.getFactor());
        }


        //显示对话框
        AddReviewCommentUI.showDialog(model, project);
    }
}
