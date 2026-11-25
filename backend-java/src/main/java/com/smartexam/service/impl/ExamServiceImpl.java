package com.smartexam.service.impl;

import com.smartexam.dto.*;
import com.smartexam.entity.*;
import com.smartexam.exception.ApiException;
import com.smartexam.repository.*;
import com.smartexam.service.ExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final CodingTemplateRepository templateRepository;
    private final CodingTestCaseRepository testCaseRepository;

    public ExamServiceImpl(ExamRepository examRepository,
                           QuestionRepository questionRepository,
                           OptionRepository optionRepository,
                           CodingTemplateRepository templateRepository,
                           CodingTestCaseRepository testCaseRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.templateRepository = templateRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Override
    public List<Exam> getAllExams() {
        return examRepository.findAllByOrderByExamIdDesc();
    }

    @Override
    public Exam getExamById(Integer examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new ApiException("Exam not found with id: " + examId, 404));
    }

    @Override
    @Transactional
    public Exam createExam(ExamRequest request) {
        Exam exam = new Exam();
        exam.setSubject(request.getSubject());

        LocalDateTime date = LocalDateTime.now();
        if (request.getExamDate() != null && !request.getExamDate().trim().isEmpty()) {
            try {
                String dStr = request.getExamDate().replace("T", " ").trim();
                if (dStr.length() == 16) {
                    dStr += ":00";
                }
                date = LocalDateTime.parse(dStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception e) {
                try {
                    date = LocalDateTime.parse(request.getExamDate().replace("T", " "));
                } catch (Exception ignored) {}
            }
        }
        exam.setExamDate(date);
        exam.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);
        exam.setTotalMarks(request.getTotalMarks() != null ? request.getTotalMarks() : 100);
        exam.setNegativeMarking(request.getNegativeMarking() != null ? request.getNegativeMarking() : BigDecimal.ZERO);

        return examRepository.save(exam);
    }

    @Override
    public List<QuestionResponseDTO> getQuestionsForExam(Integer examId) {
        List<Question> questions = questionRepository.findByExamExamId(examId);
        List<QuestionResponseDTO> result = new ArrayList<>();

        for (Question q : questions) {
            QuestionResponseDTO dto = new QuestionResponseDTO();
            dto.setQuestionId(q.getQuestionId());
            dto.setExamId(examId);
            dto.setQuestionText(q.getQuestionText());
            dto.setQuestionType(q.getQuestionType() != null ? q.getQuestionType() : "MCQ");
            dto.setDifficulty(q.getDifficulty());
            dto.setTopic(q.getTopic());
            dto.setMarks(q.getMarks());
            dto.setProblemStatement(q.getProblemStatement());
            dto.setConstraints(q.getConstraints());
            dto.setInputFormat(q.getInputFormat());
            dto.setOutputFormat(q.getOutputFormat());

            if ("CODING".equalsIgnoreCase(q.getQuestionType())) {
                // Fetch starter templates
                List<CodingTemplate> templates = templateRepository.findByQuestionQuestionId(q.getQuestionId());
                List<QuestionResponseDTO.TemplateDTO> templateDTOs = new ArrayList<>();
                for (CodingTemplate t : templates) {
                    templateDTOs.add(new QuestionResponseDTO.TemplateDTO(t.getLanguage(), t.getStarterCode()));
                }
                dto.setTemplates(templateDTOs);

                // Fetch only SAMPLE test cases (never reveal hidden ones to student)
                List<CodingTestCase> sampleCases = testCaseRepository.findByQuestionQuestionIdAndIsSampleTrue(q.getQuestionId());
                List<QuestionResponseDTO.SampleTestCaseDTO> sampleDTOs = new ArrayList<>();
                for (CodingTestCase tc : sampleCases) {
                    sampleDTOs.add(new QuestionResponseDTO.SampleTestCaseDTO(tc.getInputData(), tc.getExpectedOutput()));
                }
                dto.setSampleTestCases(sampleDTOs);
            } else {
                // Fetch MCQ options
                List<Option> options = optionRepository.findByQuestionQuestionId(q.getQuestionId());
                List<QuestionResponseDTO.OptionDTO> optionDTOs = new ArrayList<>();
                for (Option opt : options) {
                    optionDTOs.add(new QuestionResponseDTO.OptionDTO(opt.getOptionId(), opt.getOptionText()));
                }
                dto.setOptions(optionDTOs);
            }

            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional
    public void addQuestionToExam(Integer examId, QuestionRequest request) {
        Exam exam = getExamById(examId);

        Question question = new Question();
        question.setExam(exam);
        question.setQuestionText(request.getText());
        question.setMarks(request.getMarks() != null ? request.getMarks() : 1);
        question.setQuestionType(request.getQuestionType() != null ? request.getQuestionType() : "MCQ");
        question.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : "MEDIUM");
        question.setTopic(request.getTopic() != null ? request.getTopic() : "General");
        question.setProblemStatement(request.getProblemStatement());
        question.setConstraints(request.getConstraints());
        question.setInputFormat(request.getInputFormat());
        question.setOutputFormat(request.getOutputFormat());

        Question savedQuestion = questionRepository.save(question);

        if ("CODING".equalsIgnoreCase(request.getQuestionType())) {
            // Save templates
            if (request.getTemplates() != null) {
                for (TemplateRequest tr : request.getTemplates()) {
                    CodingTemplate ct = new CodingTemplate();
                    ct.setQuestion(savedQuestion);
                    ct.setLanguage(tr.getLanguage());
                    ct.setStarterCode(tr.getStarterCode());
                    templateRepository.save(ct);
                }
            }
            // Save test cases
            if (request.getTestCases() != null) {
                for (TestCaseRequest tcr : request.getTestCases()) {
                    CodingTestCase tc = new CodingTestCase();
                    tc.setQuestion(savedQuestion);
                    tc.setInputData(tcr.getInputData());
                    tc.setExpectedOutput(tcr.getExpectedOutput());
                    tc.setIsSample(tcr.getIsSample() != null && tcr.getIsSample());
                    tc.setWeightage(tcr.getWeightage() != null ? tcr.getWeightage() : 1);
                    testCaseRepository.save(tc);
                }
            }
        } else {
            // Save MCQ options
            if (request.getOptions() != null) {
                for (OptionRequest optReq : request.getOptions()) {
                    Option opt = new Option();
                    opt.setQuestion(savedQuestion);
                    opt.setOptionText(optReq.getText());
                    opt.setIsCorrect(optReq.getIsCorrect() != null && optReq.getIsCorrect());
                    optionRepository.save(opt);
                }
            }
        }
    }
}
