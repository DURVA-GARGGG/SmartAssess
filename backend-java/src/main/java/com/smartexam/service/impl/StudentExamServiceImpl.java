package com.smartexam.service.impl;

import com.smartexam.dto.ResponseRequest;
import com.smartexam.entity.*;
import com.smartexam.exception.ApiException;
import com.smartexam.repository.*;
import com.smartexam.service.StudentExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StudentExamServiceImpl implements StudentExamService {

    private final StudentExamRepository studentExamRepository;
    private final UserRepository userRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final StudentResponseRepository responseRepository;
    private final CodingSubmissionRepository submissionRepository;
    private final ResultRepository resultRepository;
    private final ProctoringLogRepository proctoringLogRepository;

    public StudentExamServiceImpl(StudentExamRepository studentExamRepository,
                                  UserRepository userRepository,
                                  ExamRepository examRepository,
                                  QuestionRepository questionRepository,
                                  OptionRepository optionRepository,
                                  StudentResponseRepository responseRepository,
                                  CodingSubmissionRepository submissionRepository,
                                  ResultRepository resultRepository,
                                  ProctoringLogRepository proctoringLogRepository) {
        this.studentExamRepository = studentExamRepository;
        this.userRepository = userRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.responseRepository = responseRepository;
        this.submissionRepository = submissionRepository;
        this.resultRepository = resultRepository;
        this.proctoringLogRepository = proctoringLogRepository;
    }

    @Override
    @Transactional
    public Integer startExam(Integer userId, Integer examId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found with id: " + userId, 404));
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ApiException("Exam not found with id: " + examId, 404));

        Optional<StudentExam> existing = studentExamRepository.findByUserUserIdAndExamExamId(userId, examId);
        if (existing.isPresent()) {
            StudentExam se = existing.get();
            if ("COMPLETED".equalsIgnoreCase(se.getStatus())) {
                throw new ApiException("You have already completed this exam.", 400);
            }
            return se.getAttemptId();
        }

        StudentExam attempt = new StudentExam();
        attempt.setUser(user);
        attempt.setExam(exam);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setStatus("IN_PROGRESS");

        StudentExam saved = studentExamRepository.save(attempt);

        // Initialize proctoring log
        ProctoringLog log = new ProctoringLog();
        log.setStudentExam(saved);
        log.setLoginTime(LocalDateTime.now());
        log.setTabSwitchCount(0);
        log.setSuspiciousFlag(false);
        proctoringLogRepository.save(log);

        return saved.getAttemptId();
    }

    @Override
    @Transactional
    public void saveResponse(ResponseRequest request) {
        if (request.getAttemptId() == null || request.getQuestionId() == null || request.getSelectedOptionId() == null) {
            throw new ApiException("Missing response fields");
        }

        StudentExam attempt = studentExamRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new ApiException("Attempt not found: " + request.getAttemptId(), 404));
        if (attempt.isClosedForAnswers(60)) {
            throw new ApiException("Time is up for this exam. Please submit.", 403);
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ApiException("Question not found: " + request.getQuestionId(), 404));

        Option option = optionRepository.findById(request.getSelectedOptionId())
                .orElseThrow(() -> new ApiException("Option not found: " + request.getSelectedOptionId(), 404));

        Optional<StudentResponse> existing = responseRepository
                .findByStudentExamAttemptIdAndQuestionQuestionId(request.getAttemptId(), request.getQuestionId());

        StudentResponse resp = existing.orElseGet(StudentResponse::new);
        resp.setStudentExam(attempt);
        resp.setQuestion(question);
        resp.setSelectedOption(option);
        resp.setResponseTime(LocalDateTime.now());

        responseRepository.save(resp);
    }

    @Override
    @Transactional
    public void submitExam(Integer attemptId) {
        StudentExam attempt = studentExamRepository.findById(attemptId)
                .orElseThrow(() -> new ApiException("Attempt not found: " + attemptId, 404));

        attempt.setStatus("COMPLETED");
        attempt.setEndTime(LocalDateTime.now());
        studentExamRepository.save(attempt);

        Exam exam = attempt.getExam();
        BigDecimal negativePenalty = exam.getNegativeMarking() != null ? exam.getNegativeMarking() : BigDecimal.ZERO;

        // 1. Calculate MCQ score
        BigDecimal mcqScore = BigDecimal.ZERO;
        List<StudentResponse> responses = responseRepository.findByStudentExamAttemptId(attemptId);
        for (StudentResponse r : responses) {
            if (r.getSelectedOption() != null) {
                if (Boolean.TRUE.equals(r.getSelectedOption().getIsCorrect())) {
                    int marks = r.getQuestion().getMarks() != null ? r.getQuestion().getMarks() : 1;
                    mcqScore = mcqScore.add(BigDecimal.valueOf(marks));
                } else {
                    mcqScore = mcqScore.subtract(negativePenalty);
                }
            }
        }

        // 2. Calculate Coding score
        BigDecimal codingScore = BigDecimal.ZERO;
        List<Question> questions = questionRepository.findByExamExamId(exam.getExamId());
        for (Question q : questions) {
            if ("CODING".equalsIgnoreCase(q.getQuestionType())) {
                Optional<CodingSubmission> bestSub = submissionRepository
                        .findTopByStudentExamAttemptIdAndQuestionQuestionIdOrderByScoreObtainedDescSubmittedAtDesc(
                                attemptId, q.getQuestionId());
                if (bestSub.isPresent() && bestSub.get().getScoreObtained() != null) {
                    codingScore = codingScore.add(bestSub.get().getScoreObtained());
                }
            }
        }

        BigDecimal totalScore = mcqScore.add(codingScore);
        if (totalScore.compareTo(BigDecimal.ZERO) < 0) {
            totalScore = BigDecimal.ZERO;
        }

        // 3. Save or Update Result
        Optional<Result> existingResult = resultRepository.findByStudentExamAttemptId(attemptId);
        Result result = existingResult.orElseGet(Result::new);
        result.setStudentExam(attempt);
        result.setScore(totalScore);

        BigDecimal passingThreshold = BigDecimal.valueOf(exam.getTotalMarks()).multiply(BigDecimal.valueOf(0.40));
        // More than 5 tab switches flags the attempt, and a flagged attempt's result is INVALID
        boolean flagged = proctoringLogRepository.findByStudentExamAttemptId(attemptId)
                .map(l -> Boolean.TRUE.equals(l.getSuspiciousFlag()))
                .orElse(false);
        if (flagged) {
            result.setResultStatus("INVALID");
        } else {
            result.setResultStatus(totalScore.compareTo(passingThreshold) >= 0 ? "PASS" : "FAIL");
        }

        resultRepository.save(result);

        // Update proctoring logout time
        proctoringLogRepository.findByStudentExamAttemptId(attemptId).ifPresent(log -> {
            log.setLogoutTime(LocalDateTime.now());
            proctoringLogRepository.save(log);
        });
    }

    @Override
    public List<Map<String, Object>> getStudentResults(Integer userId) {
        List<Result> results = resultRepository.findByUserId(userId);
        List<Map<String, Object>> list = new ArrayList<>();

        for (Result r : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("score", r.getScore());
            map.put("subject", r.getStudentExam() != null && r.getStudentExam().getExam() != null ?
                    r.getStudentExam().getExam().getSubject() : "Exam");
            map.put("result_status", r.getResultStatus());
            list.add(map);
        }

        return list;
    }
}
