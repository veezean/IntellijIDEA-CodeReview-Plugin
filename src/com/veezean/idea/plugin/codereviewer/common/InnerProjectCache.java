package com.veezean.idea.plugin.codereviewer.common;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.action.ManageReviewCommentUI;
import com.veezean.idea.plugin.codereviewer.model.CodeReviewCommentCache;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * project内需要使用的缓存对象
 *
 * @author Wang Weiren
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
        CodeReviewCommentCache deserializeCache = DataPersistentUtil.deserialize(this.project);
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
        return results.stream().sorted((o1, o2) -> (int) (Long.parseLong(o2.getId()) - Long.parseLong(o1.getId())))
                .collect(Collectors.toList());
    }

    public ReviewComment getCachedCommentById(String id) {
        return cacheData.getComments().get(id);
    }

    public String getProjectHash() {
        return this.project.getLocationHash();
    }

    public ReviewComment getLastCommentModel() {
        return cacheData.getLastCommentData();
    }

    private void updateLastCommentModel(ReviewComment model) {
        cacheData.setLastCommentData(model);
        DataPersistentUtil.serialize(cacheData, this.project);
    }

    public int addNewComment(ReviewComment commentInfo) {
        if (commentInfo == null) {
            return 0;
        }

        cacheData.getComments().put(commentInfo.getId(), commentInfo);
        DataPersistentUtil.serialize(cacheData, this.project);

        updateLastCommentModel(commentInfo);
        return 1;
    }

    public int importComments(List<ReviewComment> models) {
        if (models == null) {
            return 0;
        }

        Map<String, ReviewComment> comments = cacheData.getComments();
        for (ReviewComment model : models) {
            comments.put(model.getId(), model);
        }

        DataPersistentUtil.serialize(cacheData, this.project);
        return models.size();
    }

    public int updateCommonColumnContent(ReviewComment commentInfo) {
        if (commentInfo == null) {
            return 0;
        }

        Map<String, ReviewComment> comments = cacheData.getComments();
        if (comments == null || comments.isEmpty()) {
            return 0;
        }

        if (!comments.containsKey(commentInfo.getId())) {
            return 0;
        }

        // 只更新允许编辑的字段内容
        ReviewComment reviewComment = comments.get(commentInfo.getId());
        GlobalConfigManager.getInstance().getCustomConfigColumns().getColumns().stream()
                .filter(Column::isEditable)
                .forEach(column -> {
                    reviewComment.setPropValue(column.getColumnCode(), commentInfo.getPropValue(column.getColumnCode()));
                });

//        // 更新所有字段内容（不允许更新的字段，界面已经禁止修改了，此处直接更新全部即可）
//        // 此法不可行，会导致一些隐藏字段被更新而值丢失。弃用，还是使用上面的逻辑逐个字段更新
//        ReviewCommentInfoModel model = comments.get(commentInfo.getIdentifier());
//        BeanUtil.copyProperties(commentInfo, model);

        DataPersistentUtil.serialize(cacheData, this.project);

        // 更新无需操作pathMap，因为指针对应的具体对象是同一个，这个地方修改了，pathMap里面也就变了

        return 1;
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

        DataPersistentUtil.serialize(cacheData, this.project);
        return deleteCount;
    }

    public int clearComments() {
        Map<String, ReviewComment> comments = cacheData.getComments();
        if (comments == null || comments.isEmpty()) {
            return 0;
        }

        int size = comments.size();
        comments.clear();

        DataPersistentUtil.serialize(cacheData, this.project);
        return size;
    }

    public ManageReviewCommentUI getManageReviewCommentUI() {
        return manageReviewCommentUI;
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
}
