package com.smartexam.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "results")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Integer resultId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attempt_id")
    private StudentExam studentExam;

    @Column(name = "score", precision = 10, scale = 2)
    private BigDecimal score;

    @Column(name = "student_rank")
    private Integer studentRank;

    @Column(name = "result_status", length = 50)
    private String resultStatus;

    public Result() {}

    public Result(Integer resultId, StudentExam studentExam, BigDecimal score, Integer studentRank, String resultStatus) {
        this.resultId = resultId;
        this.studentExam = studentExam;
        this.score = score;
        this.studentRank = studentRank;
        this.resultStatus = resultStatus;
    }

    public Integer getResultId() {
        return resultId;
    }

    public void setResultId(Integer resultId) {
        this.resultId = resultId;
    }

    public StudentExam getStudentExam() {
        return studentExam;
    }

    public void setStudentExam(StudentExam studentExam) {
        this.studentExam = studentExam;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Integer getStudentRank() {
        return studentRank;
    }

    public void setStudentRank(Integer studentRank) {
        this.studentRank = studentRank;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }
}
