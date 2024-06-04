package com.veezean.idea.plugin.codereviewer.model;

import cn.hutool.core.io.file.FileNameUtil;
import com.veezean.idea.plugin.codereviewer.common.CodeReviewException;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 评审意见信息
 *
 * @author Veezean
 * @since 2022/5/22
 */
public class ReviewComment implements Serializable {

    private static final long serialVersionUID = 90179667808241147L;
    // 服务端交互使用，数据版本，CAS策略控制
    private long dataVersion;
    private int startLine;
    private int endLine;
    private Map<String, ValuePair> propValues = new HashMap<>();

    public long getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(long dataVersion) {
        this.dataVersion = dataVersion;
    }

    public Map<String, ValuePair> getPropValues() {
        return propValues;
    }

    public void setPropValues(Map<String, ValuePair> propValues) {
        this.propValues = propValues;
    }

    public String getStringPropValue(String propName) {
        ValuePair valuePair = propValues.get(propName);
        if (valuePair == null) {
            return null;
        }
        return valuePair.getStringValue();
    }

    public ValuePair getPairPropValue(String propName) {
        return propValues.get(propName);
    }

    public void setStringPropValue(String name, String propValue) {
        propValues.put(name, ValuePair.buildPair(propValue));
    }

    public void setPairPropValue(String name, ValuePair propValue) {
        propValues.put(name, propValue);
    }

    public String getId() {
        return getStringPropValue("identifier");
    }

    public void setId(String id) {
        setStringPropValue("identifier", id);
    }

    public String getFilePath() {
        return getStringPropValue("filePath");
    }

    public void setFilePath(String filePath) {
        setStringPropValue("filePath", filePath);
    }


    public String getGitRepositoryName() {
        return getStringPropValue("gitRepositoryName");
    }

    public void setGitRepositoryName(String gitRepositoryName) {
        setStringPropValue("gitRepositoryName", gitRepositoryName);
    }


    public String getGitBranchName() {
        return getStringPropValue("gitBranchName");
    }

    public void setGitBranchName(String gitBranchName) {
        setStringPropValue("gitBranchName", gitBranchName);
    }

    public String getComment() {
        return getStringPropValue("comment");
    }

    public void setComment(String comment) {
        propValues.put("comment", ValuePair.buildRawPair(comment));
    }

    public String getContent() {
        return getStringPropValue("content");
    }

    public void setContent(String content) {
        propValues.put("content", ValuePair.buildRawPair(content));
    }

    public String getCommitDate() {
        return getStringPropValue("reviewDate");
    }

    public void setCommitDate(String commitDate) {
        setStringPropValue("reviewDate", commitDate);
    }

    public void setRealConfirmer(ValuePair confirmer) {
        setPairPropValue("realConfirmer", confirmer);
    }

    public void setReviewer(ValuePair confirmer) {
        setPairPropValue("reviewer", confirmer);
    }

    public void setConfirmDate(String confirmDate) {
        setStringPropValue("confirmDate", confirmDate);
    }

    public void setConfirmResult(ValuePair confirmResult) {
        setPairPropValue("confirmResult", confirmResult);
    }

    public String getConfirmResult() {
        return getStringPropValue("confirmResult");
    }

    public String getFileSnapshot() {
        return getStringPropValue("fileSnapshot");
    }

    public void setFileSnapshot(String fileSnapshot) {
        propValues.put("fileSnapshot", ValuePair.buildRawPair(fileSnapshot));
    }

    public boolean lineMatched(int currentLine) {
        if (startLine > currentLine || endLine < currentLine) {
            // 范围没有交集
            return false;
        }
        return true;
    }

    public String getLineRange() {
        return getStringPropValue("lineRange");
    }

    public void setLineRange(int startLine, int endLine) {
        // 先记录真实的行号（从0计数）
        this.startLine = startLine;
        this.endLine = endLine;

        // 转换为人类可读的数字，与IDEA现实的行号保持一致，从1计数
        int start = startLine + 1;
        int end = endLine + 1;
        String lineRange = start + " ~ " + end;
        setStringPropValue("lineRange", lineRange);


    }

    public void setLineRangeInfo() {
        String lineRange = getStringPropValue("lineRange");
        if (StringUtils.isNotEmpty(lineRange)) {
            String[] lines = lineRange.split("~");
            if (lines.length == 2) {
                try {
                    this.startLine = Integer.parseInt(lines[0].trim());
                    this.endLine = Integer.parseInt(lines[1].trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String fileSuffix() {
        String suffix = "";
        String filePath = getFilePath();
        if (StringUtils.isNotEmpty(filePath)) {
            suffix = FileNameUtil.getSuffix(filePath);
        }
        return suffix;
    }

    public FileShortInfo getFileShortInfo() {
        String filePath = getFilePath();
        String packageName = "";
        try {
            String[] splitFilePath = filePath.split("\\,");
            if (splitFilePath.length > 1) {
                packageName = splitFilePath[0];
                filePath = splitFilePath[1];
            }
        } catch (Exception e) {
            throw new CodeReviewException("parse filePath error", e);
        }
        FileShortInfo fileShortInfo = new FileShortInfo();
        fileShortInfo.setFileName(filePath);
        fileShortInfo.setPackageName(packageName);
        return fileShortInfo;
    }
}
