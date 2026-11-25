package com.smartexam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "coding_templates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"question_id", "language"})
})
public class CodingTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Integer templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore
    private Question question;

    @Column(name = "language", nullable = false, length = 30)
    private String language;

    @Column(name = "starter_code", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String starterCode;

    public CodingTemplate() {}

    public CodingTemplate(Integer templateId, Question question, String language, String starterCode) {
        this.templateId = templateId;
        this.question = question;
        this.language = language;
        this.starterCode = starterCode;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
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
