package com.smartexam.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class QuestionResponseDTO {
    @JsonProperty("question_id")
    private Integer questionId;

    @JsonProperty("exam_id")
    private Integer examId;

    @JsonProperty("question_text")
    private String questionText;

    @JsonProperty("question_type")
    private String questionType;

    private String difficulty;
    private String topic;
    private Integer marks;

    @JsonProperty("problem_statement")
    private String problemStatement;

    private String constraints;

    @JsonProperty("input_format")
    private String inputFormat;

    @JsonProperty("output_format")
    private String outputFormat;

    private List<OptionDTO> options = new ArrayList<>();
    private List<TemplateDTO> templates = new ArrayList<>();

    @JsonProperty("sample_test_cases")
    private List<SampleTestCaseDTO> sampleTestCases = new ArrayList<>();

    public static class OptionDTO {
        @JsonProperty("option_id")
        private Integer optionId;

        @JsonProperty("option_text")
        private String optionText;

        public OptionDTO() {}

        public OptionDTO(Integer optionId, String optionText) {
            this.optionId = optionId;
            this.optionText = optionText;
        }

        public Integer getOptionId() {
            return optionId;
        }

        public void setOptionId(Integer optionId) {
            this.optionId = optionId;
        }

        public String getOptionText() {
            return optionText;
        }

        public void setOptionText(String optionText) {
            this.optionText = optionText;
        }
    }

    public static class TemplateDTO {
        private String language;
        @JsonProperty("starter_code")
        private String starterCode;

        public TemplateDTO() {}

        public TemplateDTO(String language, String starterCode) {
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

    public static class SampleTestCaseDTO {
        @JsonProperty("input_data")
        private String inputData;

        @JsonProperty("expected_output")
        private String expectedOutput;

        public SampleTestCaseDTO() {}

        public SampleTestCaseDTO(String inputData, String expectedOutput) {
            this.inputData = inputData;
            this.expectedOutput = expectedOutput;
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
    }

    public QuestionResponseDTO() {}

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getExamId() {
        return examId;
    }

    public void setExamId(Integer examId) {
        this.examId = examId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
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

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
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

    public List<OptionDTO> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDTO> options) {
        this.options = options;
    }

    public List<TemplateDTO> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateDTO> templates) {
        this.templates = templates;
    }

    public List<SampleTestCaseDTO> getSampleTestCases() {
        return sampleTestCases;
    }

    public void setSampleTestCases(List<SampleTestCaseDTO> sampleTestCases) {
        this.sampleTestCases = sampleTestCases;
    }
}
