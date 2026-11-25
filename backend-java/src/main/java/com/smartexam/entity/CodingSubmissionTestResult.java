package com.smartexam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "coding_submission_test_results")
public class CodingSubmissionTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    @JsonIgnore
    private CodingSubmission submission;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "test_case_id", nullable = false)
    private CodingTestCase testCase;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // PASSED, FAILED, TIMEOUT, ERROR

    @Column(name = "actual_output", columnDefinition = "MEDIUMTEXT")
    private String actualOutput;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs = 0;

    @Column(name = "memory_kb")
    private Integer memoryKb = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public CodingSubmissionTestResult() {}

    public Integer getDetailId() {
        return detailId;
    }

    public void setDetailId(Integer detailId) {
        this.detailId = detailId;
    }

    public CodingSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(CodingSubmission submission) {
        this.submission = submission;
    }

    public CodingTestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(CodingTestCase testCase) {
        this.testCase = testCase;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
