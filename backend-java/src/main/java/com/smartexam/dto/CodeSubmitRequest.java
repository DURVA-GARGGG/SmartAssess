package com.smartexam.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class CodeSubmitRequest {
    @JsonAlias({"attempt_id", "attemptId"})
    private Integer attemptId;

    @JsonAlias({"question_id", "questionId"})
    private Integer questionId;

    private String language;

    @JsonAlias({"source_code", "sourceCode"})
    private String sourceCode;

    public CodeSubmitRequest() {}

    public CodeSubmitRequest(Integer attemptId, Integer questionId, String language, String sourceCode) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.language = language;
        this.sourceCode = sourceCode;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
