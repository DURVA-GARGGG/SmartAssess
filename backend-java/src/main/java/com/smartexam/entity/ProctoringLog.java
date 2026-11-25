package com.smartexam.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proctoring_log")
public class ProctoringLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    private StudentExam studentExam;

    @Column(name = "login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime = LocalDateTime.now();

    @Column(name = "logout_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logoutTime;

    @Column(name = "tab_switch_count")
    private Integer tabSwitchCount = 0;

    @Column(name = "suspicious_flag")
    private Boolean suspiciousFlag = false;

    public ProctoringLog() {}

    public ProctoringLog(Integer logId, StudentExam studentExam, LocalDateTime loginTime, Integer tabSwitchCount, Boolean suspiciousFlag) {
        this.logId = logId;
        this.studentExam = studentExam;
        this.loginTime = loginTime;
        this.tabSwitchCount = tabSwitchCount;
        this.suspiciousFlag = suspiciousFlag;
    }

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public StudentExam getStudentExam() {
        return studentExam;
    }

    public void setStudentExam(StudentExam studentExam) {
        this.studentExam = studentExam;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(LocalDateTime logoutTime) {
        this.logoutTime = logoutTime;
    }

    public Integer getTabSwitchCount() {
        return tabSwitchCount;
    }

    public void setTabSwitchCount(Integer tabSwitchCount) {
        this.tabSwitchCount = tabSwitchCount;
    }

    public Boolean getSuspiciousFlag() {
        return suspiciousFlag;
    }

    public void setSuspiciousFlag(Boolean suspiciousFlag) {
        this.suspiciousFlag = suspiciousFlag;
    }
}
