package com.smartexam.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CodeSubmitResponse {
    private Integer submissionId;
    private String status;
    private Integer passedTestCases = 0;
    private Integer totalTestCases = 0;
    private BigDecimal scoreObtained = BigDecimal.ZERO;
    private Integer maxMarks = 0;
    private Integer executionTimeMs = 0;
    private Integer memoryKb = 0;
    private String compilerOutput;
    private List<TestCaseResultDTO> testCaseResults = new ArrayList<>();

    public CodeSubmitResponse() {}

    public Integer getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Integer submissionId) {
        this.submissionId = submissionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPassedTestCases() {
        return passedTestCases;
    }

    public void setPassedTestCases(Integer passedTestCases) {
        this.passedTestCases = passedTestCases;
    }

    public Integer getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(Integer totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public BigDecimal getScoreObtained() {
        return scoreObtained;
    }

    public void setScoreObtained(BigDecimal scoreObtained) {
        this.scoreObtained = scoreObtained;
    }

    public Integer getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(Integer maxMarks) {
        this.maxMarks = maxMarks;
    }

    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Integer executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public Integer getMemoryKb() {
        return memoryKb;
    }

    public void setMemoryKb(Integer memoryKb) {
        this.memoryKb = memoryKb;
    }

    public String getCompilerOutput() {
        return compilerOutput;
    }

    public void setCompilerOutput(String compilerOutput) {
        this.compilerOutput = compilerOutput;
    }

    public List<TestCaseResultDTO> getTestCaseResults() {
        return testCaseResults;
    }

    public void setTestCaseResults(List<TestCaseResultDTO> testCaseResults) {
        this.testCaseResults = testCaseResults;
    }
}
