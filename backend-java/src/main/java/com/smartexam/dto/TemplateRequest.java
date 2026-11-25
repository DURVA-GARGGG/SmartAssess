package com.smartexam.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class TemplateRequest {
    private String language;

    @JsonAlias({"starter_code", "starterCode"})
    private String starterCode;

    public TemplateRequest() {}

    public TemplateRequest(String language, String starterCode) {
        this.language = language;
        this.starterCode = starterCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }
}
