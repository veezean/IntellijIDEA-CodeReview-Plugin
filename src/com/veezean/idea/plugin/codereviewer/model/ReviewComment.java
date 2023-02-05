package com.veezean.idea.plugin.codereviewer.model;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 评审意见信息
 *
 * @author Wang Weiren
 * @since 2022/5/22
 */
public class ReviewComment implements Serializable {

    private static final long serialVersionUID = 90179667808241147L;

    private int startLine;
    private int endLine;

    private Map<String, String> propValues = new HashMap<>();

    public Map<String, String> getPropValues() {
        return propValues;
    }

    public void setPropValues(Map<String, String> propValues) {
        this.propValues = propValues;
    }

    public String getPropValue(String propName) {
        return propValues.get(propName);
    }

    public void setPropValue(String name, String propValue) {
        propValues.put(name, propValue);
    }

    public String getId() {
        return getPropValue("id");
    }

    public void setId(String id) {
        setPropValue("id", id);
    }

    public String getFilePath() {
        return getPropValue("filePath");
    }

    public  void setFilePath(String filePath) {
        setPropValue("filePath", filePath);
    }

    public String getComment() {
        return getPropValue("comment");
    }

    public void setComment(String comment) {
        setPropValue("comment", comment);
    }

    public String getContent() {
        return getPropValue("content");
    }

    public void setContent(String content) {
        setPropValue("content", content);
    }

    public String getCommitDate() {
        return getPropValue("commitDate");
    }

    public void setCommitDate(String commitDate) {
        setPropValue("commitDate", commitDate);
    }

    public boolean lineMatched(int currentLine) {
        if (startLine > currentLine || endLine < currentLine) {
            // 范围没有交集
            return false;
        }
        return true;
    }

    public String getLineRange() {
        return getPropValue("lineRange");
    }
    public void setLineRange(int startLine, int endLine) {
            int start = startLine + 1;
            int end = endLine + 1;
            String lineRange = start + " ~ " + end;
            setPropValue("lineRange", lineRange);

            this.startLine = start;
            this.endLine = end;
    }

    public void setLineRangeInfo() {
        String lineRange = getPropValue("lineRange");
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
}
