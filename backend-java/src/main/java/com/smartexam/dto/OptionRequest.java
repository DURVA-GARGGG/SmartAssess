package com.smartexam.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class OptionRequest {
    private String text;

    @JsonAlias({"is_correct", "isCorrect"})
    private Boolean isCorrect = false;

    public OptionRequest() {}

    public OptionRequest(String text, Boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}
