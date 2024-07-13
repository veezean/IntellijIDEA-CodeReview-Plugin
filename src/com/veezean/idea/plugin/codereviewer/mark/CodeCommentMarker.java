package com.veezean.idea.plugin.codereviewer.mark;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.veezean.idea.plugin.codereviewer.common.GlobalConfigManager;
import com.veezean.idea.plugin.codereviewer.consts.Constants;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.service.ProjectLevelService;
import com.veezean.idea.plugin.codereviewer.util.Logger;

import java.util.List;

/**
 * 代码评论内容标识服务
 *
 * @author Wang Weiren
 * @since 2024/6/3
 */
public class CodeCommentMarker {

    private static final TextAttributes unconfirmedTextAttr = createUnconfirmMarkerAttr();
    private static final TextAttributes confirmedTextAttr = createConfirmedMarkerAttr();

    public static void markOpenedEditor(Project project, VirtualFile virtualFile) {
        if (virtualFile == null) {
            Logger.error("virtualFile is null");
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            String virtualFileName = virtualFile.getName();
            List<ReviewComment> cachedComments =
                    ProjectLevelService.getService(project).getProjectCache().getCachedCommentsByFile(virtualFileName);
            OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
            Editor editor = FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, true);
            if (editor == null) {
                Logger.error("editor is null, file:" + virtualFileName);
                return;
            }

            MarkupModel markupModel = editor.getMarkupModel();
            RangeHighlighter[] allHighlighters = markupModel.getAllHighlighters();
            for (RangeHighlighter highlighter : allHighlighters) {
                if (highlighter.getUserData(Constants.HIGHTLIGHT_MARKER) != null) {
                    // 清除已有标记
                    markupModel.removeHighlighter(highlighter);
                }
            }

            // 逐条标记显示内容
            for (ReviewComment comment : cachedComments) {
                markOneComment(editor, comment);
            }
        });

    }

    public static void markOneComment(Editor editor, ReviewComment commentInfoModel) {
        if (editor == null) {
            Logger.error("editor is null");
            return;
        }

        String confirmResult = commentInfoModel.getConfirmResult();
        if (confirmResult.startsWith(Constants.UNCONFIRMED)) {
            Logger.info("unconfirmed comment, mark as unconfirmed");
            doMark(editor, commentInfoModel, unconfirmedTextAttr,
                    HighlighterLayer.ADDITIONAL_SYNTAX + 500);
        } else {
            Logger.info("confirmed comment, mark as confirmed");
            doMark(editor, commentInfoModel, confirmedTextAttr,
                    HighlighterLayer.ADDITIONAL_SYNTAX + 1000);
        }

    }

    private static void doMark(Editor editor, ReviewComment commentInfoModel, TextAttributes textAttributes,
                               int layer) {

        boolean closeLineMark = GlobalConfigManager.getInstance().getGlobalConfig().isCloseLineMark();
        if (closeLineMark) {
            return;
        }

        // 标识给定的评论意见
        int lineStartOffset = editor.getDocument().getLineStartOffset(commentInfoModel.getStartLine());
        int lineEndOffset = editor.getDocument().getLineEndOffset(commentInfoModel.getEndLine());
        RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(lineStartOffset,
                lineEndOffset, layer,
                textAttributes, HighlighterTargetArea.EXACT_RANGE);
        // 本插件设置的标记信息，增加个标记位，便于后续定点清除
        highlighter.putUserData(Constants.HIGHTLIGHT_MARKER, "1");
        highlighter.setErrorStripeTooltip(commentInfoModel.getComment());
    }

    private static TextAttributes createUnconfirmMarkerAttr() {
        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setBackgroundColor(null);
        textAttributes.setEffectColor(JBColor.BLUE);
        textAttributes.setEffectType(EffectType.BOLD_LINE_UNDERSCORE);
        textAttributes.setErrorStripeColor(JBColor.BLUE);
        return textAttributes;
    }

    private static TextAttributes createConfirmedMarkerAttr() {
        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setBackgroundColor(null);
        textAttributes.setEffectColor(JBColor.GREEN);
        textAttributes.setEffectType(EffectType.WAVE_UNDERSCORE);
        textAttributes.setErrorStripeColor(JBColor.GREEN);
        return textAttributes;
    }
}
