package com.smartexam.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class CodeRunRequest {
    @JsonAlias({"question_id", "questionId"})
    private Integer questionId;

    private String language;

    @JsonAlias({"source_code", "sourceCode"})
    private String sourceCode;

    @JsonAlias({"custom_input", "customInput"})
    private String customInput;

    public CodeRunRequest() {}

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

    public String getCustomInput() {
        return customInput;
    }

    public void setCustomInput(String customInput) {
        this.customInput = customInput;
    }
}
