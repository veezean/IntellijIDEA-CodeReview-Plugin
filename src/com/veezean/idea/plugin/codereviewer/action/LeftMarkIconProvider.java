package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.veezean.idea.plugin.codereviewer.common.InnerProjectCache;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 评审信息在主窗口左侧显示对应标记的处理逻辑
 *
 * @author Veezean
 * @since 2019/10/1
 */
public class LeftMarkIconProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {

        if (!(element instanceof PsiWhiteSpace)) {
            super.collectNavigationMarkers(element, result);
            return;
        }

        int textOffset = element.getTextOffset();
        int textLength = element.getTextLength();
        int textEndOffset = textOffset + textLength;

        if (textOffset < 0) {
            super.collectNavigationMarkers(element, result);
            return;
        }

        PsiFile containingFile = element.getContainingFile();
        Project project = element.getProject();
        Document document = PsiDocumentManager.getInstance(project).getDocument(containingFile);
        if (document != null) {
            int startLineNumber = document.getLineNumber(textOffset);
            int endLineNumber = document.getLineNumber(textEndOffset);

            // 同一行内的空格重复匹配，不处理，直接忽略
            if (startLineNumber == endLineNumber) {
                super.collectNavigationMarkers(element, result);
                return;
            }

            // currentLine统一用endLine来处理，标准化所有处理场景，避免换行的场景，上下都被匹配上了
            int currentLine = endLineNumber - 1;
            InnerProjectCache projectCache = ProjectLevelService.getService(project).getProjectCache();
            if (projectCache != null) {
                String path = CommonUtil.getFileFullName(element.getContainingFile());

                String comment = projectCache.getCommentInfo(path, currentLine);
                if (comment != null) {
                    NavigationGutterIconBuilder<PsiElement> builder =
                            NavigationGutterIconBuilder.create(CommonUtil.getDefaultIcon());
                    builder.setTarget(element);
                    builder.setTooltipText(comment);
                    result.add(builder.createLineMarkerInfo(element));
                    return;
                }
            }
        }

        super.collectNavigationMarkers(element, result);
    }
}
