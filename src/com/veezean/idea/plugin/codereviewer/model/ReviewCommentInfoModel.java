package com.veezean.idea.plugin.codereviewer.model;

import cn.hutool.core.util.StrUtil;

import java.io.Serializable;

/**
 * 评审信息实体对象
 *
 * @author Wang Weiren
 * @date 2021/4/25
 */
public class ReviewCommentInfoModel implements Serializable {
    private static final long serialVersionUID = -5134323185285399922L;
    private long identifier;
    private String reviewer;
    private String handler; // 确认人
    private String comments;
    private String filePath;
    /**
     *   start ~ end的格式，用于显示
     *   运算的时候，行号是从0计算的，因此显示的时候，start和end在实际下标上+1
     */
    private String lineRange;
    private int startLine;
    private int endLine;
    private String content;
    private String type;
    private String severity;
    private String factor;
    private String dateTime;
    private String projectVersion; // 项目版本
    private String belongIssue; // 相关需求

    private String confirmResult; // 确认结果， 未确认，已修改，待修改，拒绝
    private String confirmNotes; // 确认备注

    // [网络版本扩展字段]用于记录DB中唯一ID，供后续扩展更新场景使用
    private long entityUniqueId = -1L;

    public ReviewCommentInfoModel() {
    }


    public String getLineRange() {
        if (lineRange == null) {
            int start = startLine + 1;
            int end = endLine + 1;
            lineRange = start + " ~ " + end;
        }
        return lineRange;
    }


    public boolean lineMatched(int currentLine) {
        if (startLine > currentLine || endLine < currentLine) {
            // 范围没有交集
            return false;
        }
        return true;
    }

    public String getConfirmResult() {
        if (StrUtil.isEmpty(this.confirmResult)) {
            return "未确认";
        }
        return this.confirmResult;
    }

    public void setConfirmResult(String confirmResult) {
        if (StrUtil.isEmpty(confirmResult)) {
            this.confirmResult = "未确认";
        }
        this.confirmResult = confirmResult;
    }

    public long getIdentifier() {
        return identifier;
    }

    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setLineRange(String lineRange) {
        this.lineRange = lineRange;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getBelongIssue() {
        return belongIssue;
    }

    public void setBelongIssue(String belongIssue) {
        this.belongIssue = belongIssue;
    }

    public String getConfirmNotes() {
        return confirmNotes;
    }

    public void setConfirmNotes(String confirmNotes) {
        this.confirmNotes = confirmNotes;
    }

    public long getEntityUniqueId() {
        return entityUniqueId;
    }

    public void setEntityUniqueId(long entityUniqueId) {
        this.entityUniqueId = entityUniqueId;
    }
}