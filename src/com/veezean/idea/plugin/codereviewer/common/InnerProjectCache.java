package com.veezean.idea.plugin.codereviewer.common;

import cn.hutool.core.util.ObjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.veezean.idea.plugin.codereviewer.action.ManageReviewCommentUI;
import com.veezean.idea.plugin.codereviewer.consts.Constants;
import com.veezean.idea.plugin.codereviewer.consts.InputTypeDefine;
import com.veezean.idea.plugin.codereviewer.model.CodeReviewCommentCache;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.model.ValuePair;
import com.veezean.idea.plugin.codereviewer.util.CommonUtil;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.apache.commons.lang.StringUtils;

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
    private VirtualFile currentOpenedEditorFile;


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

    public void recordCurrentOpenedEditFile(VirtualFile virtualFile) {
        this.currentOpenedEditorFile = virtualFile;
    }

    /**
     * 获取当前打开的窗口
     *
     * @return
     */
    public VirtualFile getCurrentOpenedEditorFile() {
        return this.currentOpenedEditorFile;
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

    public List<ReviewComment> getCachedCommentsByFile(String fileName) {
        try {
            Map<String, ReviewComment> cachedComments = cacheData.getComments();
            List<ReviewComment> results = new ArrayList<>();
            cachedComments.forEach((id, commentInfoModel) -> results.add(commentInfoModel));
            return results.stream()
                    .filter(reviewComment -> StringUtils.equals(reviewComment.getFileShortInfo().getFileName(),
                            fileName))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            Logger.error("failed to get cached comments by fileName", e);
        }
        return new ArrayList<>();
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

//    public void updateCommonColumnContent(ReviewComment commentInfo) {
//        if (commentInfo == null) {
//            return;
//        }
//
//        Map<String, ReviewComment> comments = cacheData.getComments();
//        if (comments == null || comments.isEmpty()) {
//            return;
//        }
//
//        if (!comments.containsKey(commentInfo.getId())) {
//            return;
//        }
//        // 只更新允许编辑的字段内容
//        ReviewComment reviewComment = comments.get(commentInfo.getId());
//        GlobalConfigManager.getInstance().getCustomConfigColumns().getColumns().stream()
//                .filter(column -> column.isEditableInAddPage() || column.isEditableInEditPage() || column
//                .isEditableInConfirmPage())
//                .forEach(column -> {
//                    if (InputTypeDefine.isComboBox(column.getInputType())) {
//                        reviewComment.setPairPropValue(column.getColumnCode(),
//                                commentInfo.getPairPropValue(column.getColumnCode()));
//                    } else {
//                        reviewComment.setStringPropValue(column.getColumnCode(),
//                                commentInfo.getStringPropValue(column.getColumnCode()));
//                    }
//
//                });
//        serialize(cacheData, this.project);
//    }


    public boolean updateCommonColumnContent(String targetId, Column targetColumnDefine, Object columnValue,
                                             Runnable handleConfirmResultExtraOperate) {
        if (targetColumnDefine == null) {
            Logger.error("column define is null, do not know which column to modify");
            return false;
        }

        if (!targetColumnDefine.isEditableInAddPage()
                && !targetColumnDefine.isEditableInEditPage()
                && !targetColumnDefine.isEditableInConfirmPage()) {
            Logger.error("current column cannot be edit:" + targetColumnDefine.getColumnCode());
            return false;
        }

        ReviewComment existComment = cacheData.getComments().get(targetId);
        if (existComment == null) {
            Logger.error("cannot find the exist comment with id:" + targetId);
            return false;
        }

        // 比较，只有内容变更的时候，才执行更新操作
        if (InputTypeDefine.isComboBox(targetColumnDefine.getInputType())) {
            ValuePair oldValue = existComment.getPairPropValue(targetColumnDefine.getColumnCode());
            if (!ObjectUtil.equals(oldValue, columnValue)) {
                ValuePair pair = (ValuePair) columnValue;
                existComment.setPairPropValue(targetColumnDefine.getColumnCode(), pair);

                // 如果是确认结果的变更，需要同步处理下确认时间与确认人信息
                if ("confirmResult".equals(targetColumnDefine.getColumnCode())) {
                    // 如果有具体确认结果，则自动记录对应的确认时间与确认人员
                    if (pair != null && !Constants.UNCONFIRMED.equals(pair.getValue())) {
                        existComment.setConfirmDate(CommonUtil.time2String(System.currentTimeMillis()));
                        if (GlobalConfigManager.getInstance().getGlobalConfig().isNetworkMode()) {
                            existComment.setRealConfirmer(GlobalConfigManager.getInstance().getGlobalConfig().getCurrentUserInfo());
                        }
                    } else {
                        // 清除掉确认时间与确认人员信息
                        existComment.setConfirmDate("");
                        existComment.setRealConfirmer(null);
                    }

                    // 后置执行逻辑，执行完confirmResult附加操作之后的处理逻辑
                    handleConfirmResultExtraOperate.run();
                }

                // 存储前，标记该条记录已经被修改过
                existComment.setCommitFlag(CommitFlag.UNCOMMITED);
                serialize(cacheData, this.project);
                Logger.info("column value changed, save finished. columnCode:" + targetColumnDefine.getColumnCode());
                return true;
            }
        } else {
            String oldValue = existComment.getStringPropValue(targetColumnDefine.getColumnCode());
            if (!ObjectUtil.equals(oldValue, columnValue)) {
                existComment.setStringPropValue(targetColumnDefine.getColumnCode(), (String) columnValue);

                // 存储前，标记该条记录已经被修改过
                existComment.setCommitFlag(CommitFlag.UNCOMMITED);
                serialize(cacheData, this.project);
                Logger.info("column value changed, save finished. columnCode:" + targetColumnDefine.getColumnCode());
                return true;
            }
        }

        return false;
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
