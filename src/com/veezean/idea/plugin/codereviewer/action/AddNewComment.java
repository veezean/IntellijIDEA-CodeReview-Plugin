package com.veezean.idea.plugin.codereviewer.action;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.psi.PsiFile;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.consts.Constants;
import com.veezean.idea.plugin.codereviewer.consts.InputTypeDefine;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.model.ValuePair;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import git4idea.GitBranch;
import git4idea.GitUtil;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import groovy.util.logging.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 添加评审信息操作
 *
 * @author Veezean
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

//        Project project = e.getProject();
        ProjectLevelService projectLevelService = ProjectLevelService.getService(Objects.requireNonNull(e.getProject()));
        InnerProjectCache projectCache = projectLevelService.getProjectCache();

        // 上一次的内容全部填进去，减少用户从0填写的操作
        ReviewComment lastCommentModel = projectCache.getLastCommentModel();
        if (lastCommentModel != null) {
            Map<String, Column> columnMap =
                    GlobalConfigManager.getInstance().getCustomConfigColumns().getColumns().stream()
                    .filter(Column::isShowInAddPage)
                    .collect(Collectors.toMap(Column::getColumnCode, column -> column));
            lastCommentModel.getPropValues().forEach((s, valuePair) -> {
                if (columnMap.containsKey(s)) {
                    model.setPairPropValue(s, valuePair);
                }
            });
        }

        // 特殊字段内容使用新值更新替代掉
        Document document = data.getDocument();
        int startLine = document.getLineNumber(selectionModel.getSelectionStart());
        int endLine = document.getLineNumber(selectionModel.getSelectionEnd());

        model.setLineRange(startLine, endLine);
        model.setContent(selectedText);
        model.setFilePath(classPath);
        // 当前的整个文档快照
        model.setFileSnapshot(document.getText());

        model.setComment("");
//        model.setId(RandomUtil.randomString(20));
        model.setId(IdUtil.getSnowflakeNextIdStr());

        try {
            // 如果有设置需要git相关信息，则进行读取，否则直接跳过
            boolean anyMatch = GlobalConfigManager.getInstance().getCustomConfigColumns().getColumns().stream()
                    .anyMatch(column -> {
                        String columnCode = column.getColumnCode();
                        return StringUtils.equals(columnCode, "gitRepositoryName") || StringUtils.equals(columnCode,
                                "gitBranchName");
                    });
            if (anyMatch) {
                GitRepository gitRepository = GitBranchUtil.getCurrentRepository(e.getProject());
                if (gitRepository != null) {
                    String gitBranchName = gitRepository.getCurrentBranch().findTrackedBranch(gitRepository).getName();

                    String gitRepositoryName = gitRepository.getRemotes().stream()
                            .filter(Objects::nonNull)
                            .map(GitRemote::getUrls)
                            .filter(CollectionUtils::isNotEmpty)
                            .map(url -> url.get(0))
                            .filter(StringUtils::isNotEmpty)
                            .filter(url -> url.indexOf("/") > 0)
                            .map(url -> url.substring(url.indexOf("/") + 1))
                            .findFirst()
                            .orElse("");

                    Logger.info("当前项目git仓库名称：" + gitRepositoryName + ", 分支名称：" + gitBranchName);

                    model.setGitRepositoryName(gitRepositoryName);
                    model.setGitBranchName(gitBranchName);
                } else {
                    Logger.error("Current project has no GIT repository, set git branch and repository name to empty");
                }
            }
        } catch (Exception ex) {
            Logger.error("获取git相关信息失败", ex);
        }

        //显示对话框
        ReviewCommentDialog.show(model, e.getProject(), Constants.ADD_COMMENT);

        Logger.info("新增评审意见操作窗口已经弹出");
    }
}
