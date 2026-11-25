package com.smartexam.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ProctorLogRequest {
    @JsonAlias({"attempt_id", "attemptId"})
    private Integer attemptId;

    private String action = "TAB_SWITCH";

    public ProctorLogRequest() {}

    public ProctorLogRequest(Integer attemptId, String action) {
        this.attemptId = attemptId;
        this.action = action;
    }

    public Integer getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Integer attemptId) {
        this.attemptId = attemptId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
