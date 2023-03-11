package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.common.ProjectInstanceManager;
import com.veezean.idea.plugin.codereviewer.consts.Constants;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.util.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 添加评审信息操作
 *
 * @author Wang Weiren
 * @date 2021/4/25
 */
public class AddNewComment extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        //获取当前操作的类文件
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        //获取当前类文件的路径
        String classPath = CommonUtil.getFileFullName(psiFile);

        Editor data = e.getData(CommonDataKeys.EDITOR);

        SelectionModel selectionModel = data.getSelectionModel();
        // 获取当前选择的内容
        String selectedText = selectionModel.getSelectedText();
        if (selectedText == null || "".equals(selectedText)) {
            return;
        }

        Logger.info("触发新增评审意见的操作，文件路径：" + classPath + "， 选中内容长度：" + selectedText.length());

        ReviewComment model = new ReviewComment();

        Project project = e.getProject();
        String locationHash = project.getLocationHash();
        InnerProjectCache projectCache = ProjectInstanceManager.getInstance().getProjectCache(locationHash);
        if (projectCache == null) {
            projectCache = new InnerProjectCache(project);
            ProjectInstanceManager.getInstance().addProjectCache(locationHash, projectCache);
        }

        // 上一次的内容全部填进去，减少用户从0填写的操作
        ReviewComment lastCommentModel = projectCache.getLastCommentModel();
        if (lastCommentModel != null) {
            // 剔除掉confirm界面的字段、新创建的窗口里面，confirm信息肯定都是空
            List<String> confirmProps =
                    GlobalConfigManager.getInstance().getCustomConfigColumns().getColumns().stream()
                    .filter(Column::isConfirmProp)
                    .map(Column::getColumnCode)
                    .collect(Collectors.toList());
            lastCommentModel.getPropValues().forEach((name, propValue) -> {
                if (confirmProps.contains(name)) {
                    return;
                }
                model.setPropValue(name, propValue);
            });
        }

        // 特殊字段内容使用新值更新替代掉
        Document document = data.getDocument();
        int startLine = document.getLineNumber(selectionModel.getSelectionStart());
        int endLine = document.getLineNumber(selectionModel.getSelectionEnd());

        model.setLineRange(startLine, endLine);
        model.setContent(selectedText);
        model.setFilePath(classPath);
        long currentTimeMillis = System.currentTimeMillis();
        model.setId(String.valueOf(currentTimeMillis));
        model.setCommitDate(CommonUtil.time2String(currentTimeMillis));
        model.setComment("");

        Logger.info("新增评审意见操作窗口已经弹出");

        //显示对话框
        ReviewCommentDialog.show(model, project, Constants.ADD_COMMENT);

        Logger.info("新增评审意见操作窗口已经关闭");
    }
}
