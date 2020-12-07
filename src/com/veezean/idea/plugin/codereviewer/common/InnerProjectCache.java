package com.veezean.idea.plugin.codereviewer.common;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.action.ManageReviewCommentUI;
import com.veezean.idea.plugin.codereviewer.model.CodeReviewCommentCache;
import com.veezean.idea.plugin.codereviewer.model.ReviewCommentInfoModel;

import java.util.*;

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

    public List<ReviewCommentInfoModel> getCachedComments() {
        Map<Long, ReviewCommentInfoModel> cachedComments = cacheData.getComments();
        List<ReviewCommentInfoModel> results = new ArrayList<>();
        cachedComments.forEach((aLong, commentInfoModel) -> results.add(commentInfoModel));
        return results;
    }

    public String getProjectHash() {
        return this.project.getLocationHash();
    }

    public ReviewCommentInfoModel getLastCommentModel() {
        return cacheData.getLastCommentData();
    }

    private void updateLastCommentModel(ReviewCommentInfoModel model) {
        cacheData.setLastCommentData(model);
        DataPersistentUtil.serialize(cacheData, this.project);
    }

    public int addNewComment(ReviewCommentInfoModel commentInfo) {
        if (commentInfo == null) {
            return 0;
        }

        cacheData.getComments().put(commentInfo.getIdentifier(), commentInfo);
        DataPersistentUtil.serialize(cacheData, this.project);

        updateLastCommentModel(commentInfo);
        return 1;
    }

    public int importComments(List<ReviewCommentInfoModel> models) {
        if (models == null) {
            return 0;
        }

        Map<Long, ReviewCommentInfoModel> comments = cacheData.getComments();
        for (ReviewCommentInfoModel model : models) {
            comments.put(model.getIdentifier(), model);
        }

        DataPersistentUtil.serialize(cacheData, this.project);
        return models.size();
    }

    public int updateCommonColumnContent(ReviewCommentInfoModel commentInfo) {
        if (commentInfo == null) {
            return 0;
        }

        Map<Long, ReviewCommentInfoModel> comments = cacheData.getComments();
        if (comments == null || comments.isEmpty()) {
            return 0;
        }

        if (!comments.containsKey(commentInfo.getIdentifier())) {
            return 0;
        }

        // 只更新允许编辑的字段内容
        ReviewCommentInfoModel model = comments.get(commentInfo.getIdentifier());
        model.setReviewer(commentInfo.getReviewer());
        model.setComments(commentInfo.getComments());
        model.setType(commentInfo.getType());
        model.setSeverity(commentInfo.getSeverity());
        model.setFactor(commentInfo.getFactor());

        DataPersistentUtil.serialize(cacheData, this.project);

        // 更新无需操作pathMap，因为指针对应的具体对象是同一个，这个地方修改了，pathMap里面也就变了

        return 1;
    }

    public int deleteComments(List<Long> identifierList) {
        Map<Long, ReviewCommentInfoModel> comments = cacheData.getComments();
        if (comments == null || comments.isEmpty() || identifierList == null || identifierList.isEmpty()) {
            return 0;
        }

        int deleteCount = 0;
        for (Long identifier : identifierList) {
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
        Map<Long, ReviewCommentInfoModel> comments = cacheData.getComments();
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
        String result = null;
        Map<Long, ReviewCommentInfoModel> comments = cacheData.getComments();
        Set<Map.Entry<Long, ReviewCommentInfoModel>> entries = comments.entrySet();
        ReviewCommentInfoModel value = null;
        for (Map.Entry<Long, ReviewCommentInfoModel> entry : entries) {
            value = entry.getValue();
            if (value.getFilePath().equals(filePath) && value.lineMatched(currentLine)) {
                result = value.getComments();
                break;
            }
        }

        return result;
    }
}
