package com.veezean.idea.plugin.codereviewer.common;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.action.ManageReviewCommentUI;
import com.veezean.idea.plugin.codereviewer.consts.InputTypeDefine;
import com.veezean.idea.plugin.codereviewer.model.CodeReviewCommentCache;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * project内需要使用的缓存对象
 *
 * @author Veezean
 * @since 2019/9/30
 */
public class InnerProjectCache {
    private CodeReviewCommentCache cacheData;
    private ManageReviewCommentUI manageReviewCommentUI;
    private Project project;


    public InnerProjectCache(Project project) {
        this.project = project;
        reloadCacheData();
    }

    private void reloadCacheData() {
        CodeReviewCommentCache deserializeCache = deserialize(this.project);
        if (deserializeCache == null) {
            cacheData = new CodeReviewCommentCache();
        } else {
            cacheData = deserializeCache;
        }

        if (cacheData.getComments() == null) {
            cacheData.setComments(new HashMap<>());
        }
    }

    public List<ReviewComment> getCachedComments() {
        Map<String, ReviewComment> cachedComments = cacheData.getComments();
        List<ReviewComment> results = new ArrayList<>();
        cachedComments.forEach((id, commentInfoModel) -> results.add(commentInfoModel));
        return results.stream().sorted((o1, o2) -> {
            Date date1 = CommonUtil.stringToDate(o1.getCommitDate());
            Date date2 = CommonUtil.stringToDate(o2.getCommitDate());
            return date1.before(date2) ? 1 : -1;
        })
                .collect(Collectors.toList());
    }

    public ReviewComment getCachedCommentById(String id) {
        return cacheData.getComments().get(id);
    }

    public ReviewComment getLastCommentModel() {
        return cacheData.getLastCommentData();
    }

    private void updateLastCommentModel(ReviewComment model) {
        cacheData.setLastCommentData(model);
        serialize(cacheData, this.project);
    }

    public void addNewComment(ReviewComment commentInfo) {
        if (commentInfo == null) {
            return;
        }

        cacheData.getComments().put(commentInfo.getId(), commentInfo);
        serialize(cacheData, this.project);

        updateLastCommentModel(commentInfo);
    }

    public void importComments(List<ReviewComment> models) {
        if (models == null) {
            return;
        }

        Map<String, ReviewComment> comments = cacheData.getComments();
        for (ReviewComment model : models) {
            comments.put(model.getId(), model);
        }

        serialize(cacheData, this.project);
    }

    public void updateCommonColumnContent(ReviewComment commentInfo) {
        if (commentInfo == null) {
            return;
        }

        Map<String, ReviewComment> comments = cacheData.getComments();
        if (comments == null || comments.isEmpty()) {
            return;
        }

        if (!comments.containsKey(commentInfo.getId())) {
            return;
        }
        // 只更新允许编辑的字段内容
        ReviewComment reviewComment = comments.get(commentInfo.getId());
        GlobalConfigManager.getInstance().getCustomConfigColumns().getColumns().stream()
                .filter(column -> column.isEditableInAddPage() || column.isEditableInEditPage() || column.isEditableInConfirmPage())
                .forEach(column -> {
                    if (InputTypeDefine.isComboBox(column.getInputType())) {
                        reviewComment.setPairPropValue(column.getColumnCode(),
                                commentInfo.getPairPropValue(column.getColumnCode()));
                    } else {
                        reviewComment.setStringPropValue(column.getColumnCode(),
                                commentInfo.getStringPropValue(column.getColumnCode()));
                    }

                });
        serialize(cacheData, this.project);
    }

    public int deleteComments(List<String> identifierList) {
        Map<String, ReviewComment> comments = cacheData.getComments();
        if (comments == null || comments.isEmpty() || identifierList == null || identifierList.isEmpty()) {
            return 0;
        }

        int deleteCount = 0;
        for (String identifier : identifierList) {
            if (!comments.containsKey(identifier)) {
                return 0;
            }

            comments.remove(identifier);
            deleteCount++;
        }

        serialize(cacheData, this.project);
        return deleteCount;
    }

    public int clearComments() {
        Map<String, ReviewComment> comments = cacheData.getComments();
        if (comments == null || comments.isEmpty()) {
            return 0;
        }

        int size = comments.size();
        comments.clear();

        serialize(cacheData, this.project);
        return size;
    }

    public Optional<ManageReviewCommentUI> getManageReviewCommentUI() {
        return Optional.ofNullable(manageReviewCommentUI);
    }

    public void setManageReviewCommentUI(ManageReviewCommentUI manageReviewCommentUI) {
        this.manageReviewCommentUI = manageReviewCommentUI;
    }

    public String getCommentInfo(String filePath, int currentLine) {
        try {
            String result = null;
            Map<String, ReviewComment> comments = cacheData.getComments();
            Set<Map.Entry<String, ReviewComment>> entries = comments.entrySet();
            ReviewComment value = null;
            for (Map.Entry<String, ReviewComment> entry : entries) {
                value = entry.getValue();
                if (value.getFilePath().equals(filePath) && value.lineMatched(currentLine)) {
                    result = value.getComment();
                    break;
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 序列化评审信息
     *
     * @param cache 评审信息缓存数据
     * @param project 当前项目
     */
    synchronized static void serialize(CodeReviewCommentCache cache, Project project) {
        SerializeUtils.serialize(cache, ".idea_CodeReviewHelper_data", project.getLocationHash() + "_comment.dat");
    }

    /**
     * 反序列化评审数据
     *
     * @param project 当前项目
     * @return 反序列化后的评审数据
     */
    synchronized static CodeReviewCommentCache deserialize(Project project) {
        return SerializeUtils.deserialize(".idea_CodeReviewHelper_data", project.getLocationHash() + "_comment.dat");
    }
}
