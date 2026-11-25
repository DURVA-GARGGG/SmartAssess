package com.smartexam.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Integer examId;

    @Column(name = "subject", nullable = false, length = 100)
    private String subject;

    @Column(name = "exam_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime examDate;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "negative_marking", precision = 5, scale = 2)
    private BigDecimal negativeMarking = BigDecimal.ZERO;

    public Exam() {}

    public Exam(Integer examId, String subject, LocalDateTime examDate, Integer durationMinutes, Integer totalMarks, BigDecimal negativeMarking) {
        this.examId = examId;
        this.subject = subject;
        this.examDate = examDate;
        this.durationMinutes = durationMinutes;
        this.totalMarks = totalMarks;
        this.negativeMarking = negativeMarking;
    }

    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDateTime getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDateTime examDate) {
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
