package com.smartexam.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.ArrayList;
import java.util.List;

public class QuestionRequest {
    @JsonAlias({"text", "question_text", "questionText"})
    private String text;

    private Integer marks = 1;

    @JsonAlias({"question_type", "questionType"})
    private String questionType = "MCQ"; // MCQ or CODING

    private String difficulty = "MEDIUM";
    private String topic = "General";

    @JsonAlias({"problem_statement", "problemStatement"})
    private String problemStatement;

    private String constraints;

    @JsonAlias({"input_format", "inputFormat"})
    private String inputFormat;

    @JsonAlias({"output_format", "outputFormat"})
    private String outputFormat;

    private List<OptionRequest> options = new ArrayList<>();
    private List<TemplateRequest> templates = new ArrayList<>();

    @JsonAlias({"test_cases", "testCases"})
    private List<TestCaseRequest> testCases = new ArrayList<>();

    public QuestionRequest() {}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getProblemStatement() {
        return problemStatement;
    }

    public void setProblemStatement(String problemStatement) {
        this.problemStatement = problemStatement;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public List<OptionRequest> getOptions() {
        return options;
    }

    public void setOptions(List<OptionRequest> options) {
        this.options = options;
    }

    public List<TemplateRequest> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateRequest> templates) {
        this.templates = templates;
    }

    public List<TestCaseRequest> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCaseRequest> testCases) {
        this.testCases = testCases;
    }
}
