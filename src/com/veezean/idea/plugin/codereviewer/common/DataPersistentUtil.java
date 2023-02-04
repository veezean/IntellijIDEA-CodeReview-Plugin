package com.veezean.idea.plugin.codereviewer.common;

import com.intellij.openapi.project.Project;
import com.veezean.idea.plugin.codereviewer.model.CodeReviewCommentCache;

/**
 * 数据持久化工具类
 *
 * @author Wang Weiren
 * @since 2019/10/2
 */
public class DataPersistentUtil {

    /**
     * 序列化评审信息
     *
     * @param cache 评审信息缓存数据
     * @param project 当前项目
     */
    public synchronized static void serialize(CodeReviewCommentCache cache, Project project) {
        SerializeUtils.serialize(cache, ".idea_code_review_data", project.getLocationHash() + "_comment.dat");
    }

    /**
     * 反序列化评审数据
     *
     * @param project 当前项目
     * @return 反序列化后的评审数据
     */
    public synchronized static CodeReviewCommentCache deserialize(Project project) {
        return SerializeUtils.deserialize(".idea_code_review_data", project.getLocationHash() + "_comment.dat");
    }
}
