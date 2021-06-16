package com.veezean.idea.plugin.codereviewer.model;

import java.io.Serializable;

/**
 * <类功能简要描述>
 *
 * @author Wang Weiren
 * @since 2021/6/15
 */
public class CreateIssueResponseBody implements Serializable {

    private static final long serialVersionUID = -8266769891727779353L;
    private String id;
    private String state;
    private String number;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
