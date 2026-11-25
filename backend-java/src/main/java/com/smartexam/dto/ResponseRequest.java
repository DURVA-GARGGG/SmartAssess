package com.smartexam.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ResponseRequest {
    @JsonAlias({"attempt_id", "attemptId"})
    private Integer attemptId;

    @JsonAlias({"question_id", "questionId"})
    private Integer questionId;

    @JsonAlias({"selected_option_id", "selectedOptionId"})
    private Integer selectedOptionId;

    public ResponseRequest() {}

    public ResponseRequest(Integer attemptId, Integer questionId, Integer selectedOptionId) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
    }

    public Integer getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Integer attemptId) {
        this.attemptId = attemptId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Integer selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }
}
