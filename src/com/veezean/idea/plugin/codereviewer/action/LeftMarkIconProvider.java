package com.veezean.idea.plugin.codereviewer.action;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.Icons;
import com.veezean.idea.plugin.codereviewer.common.DataPersistentUtil;
import com.veezean.idea.plugin.codereviewer.common.GlobalCacheManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2019/10/1
 */
public class LeftMarkIconProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        String path = element.getContainingFile().getVirtualFile().getName();

        if (!(element instanceof PsiWhiteSpace)) {
            super.collectNavigationMarkers(element, result);
            return;
        }

        int textOffset = element.getTextOffset();
        System.out.println("textOffset = " + textOffset);
        int textLength = element.getTextLength();
        System.out.println("textLength = " + textLength);
        int textEndOffset = textOffset + textLength;

        if (textOffset < 0) {
            super.collectNavigationMarkers(element, result);
            return;
        }


        PsiFile containingFile = element.getContainingFile();
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(containingFile);

        int startLineNumber = document.getLineNumber(textOffset);
        int endLineNumber = document.getLineNumber(textEndOffset);

        System.out.println("startLineNumber = " + startLineNumber + ", endLineNumber = " + endLineNumber);

        //TODO 看下是否封装提取下
        int projectIdentifier = DataPersistentUtil.getProjectIdentifier();
        if (projectIdentifier < 0) {
            int projectUniqueId = element.getProject().getProjectFile().toString().hashCode();
            DataPersistentUtil.setProjectIdentifier(projectUniqueId);
        }

        String comment = GlobalCacheManager.getInstance().getCommentInfo(path, startLineNumber, endLineNumber);

        if (comment == null) {
            super.collectNavigationMarkers(element, result);
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(Icons.UI_FORM_ICON);
        builder.setTarget(element);
        builder.setTooltipText(comment);
        result.add(builder.createLineMarkerInfo(element));
    }
}
