package com.smartexam.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class TestCaseRequest {
    @JsonAlias({"input_data", "inputData"})
    private String inputData;

    @JsonAlias({"expected_output", "expectedOutput"})
    private String expectedOutput;

    @JsonAlias({"is_sample", "isSample"})
    private Boolean isSample = false;

    private Integer weightage = 1;

    public TestCaseRequest() {}

    public TestCaseRequest(String inputData, String expectedOutput, Boolean isSample, Integer weightage) {
        this.inputData = inputData;
        this.expectedOutput = expectedOutput;
        this.isSample = isSample;
        this.weightage = weightage;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public Boolean getIsSample() {
        return isSample;
    }

    public void setIsSample(Boolean isSample) {
        this.isSample = isSample;
    }

    public Integer getWeightage() {
        return weightage;
    }

    public void setWeightage(Integer weightage) {
        this.weightage = weightage;
    }
}
