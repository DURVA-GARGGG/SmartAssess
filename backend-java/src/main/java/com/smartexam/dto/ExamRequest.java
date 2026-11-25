package com.smartexam.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;

public class ExamRequest {
    private String subject;

    @JsonAlias({"exam_date", "examDate"})
    private String examDate;

    @JsonAlias({"duration_minutes", "durationMinutes"})
    private Integer durationMinutes;

    @JsonAlias({"total_marks", "totalMarks"})
    private Integer totalMarks;

    @JsonAlias({"negative_marking", "negativeMarking"})
    private BigDecimal negativeMarking = BigDecimal.ZERO;

    public ExamRequest() {}

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public BigDecimal getNegativeMarking() {
        return negativeMarking;
    }

    public void setNegativeMarking(BigDecimal negativeMarking) {
        this.negativeMarking = negativeMarking;
    }
}
