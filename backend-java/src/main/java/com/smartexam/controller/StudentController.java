package com.smartexam.controller;

import com.smartexam.config.JwtUtil;
import com.smartexam.dto.QuestionResponseDTO;
import com.smartexam.dto.ResponseRequest;
import com.smartexam.entity.Exam;
import com.smartexam.exception.ApiException;
import com.smartexam.service.ExamService;
import com.smartexam.service.StudentExamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    private final ExamService examService;
    private final StudentExamService studentExamService;
    private final JwtUtil jwtUtil;

    public StudentController(ExamService examService, StudentExamService studentExamService, JwtUtil jwtUtil) {
        this.examService = examService;
        this.studentExamService = studentExamService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/exams/available")
    public ResponseEntity<List<Exam>> getAvailableExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @PostMapping("/exams/{examId}/start")
    public ResponseEntity<Map<String, Object>> startExam(
            @PathVariable Integer examId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {

        Integer userId = extractUserId(authHeader, request);
        if (userId == null) {
            userId = 2; // Default fallback student ID for open demo access
        }

        Integer attemptId = studentExamService.startExam(userId, examId);
        return ResponseEntity.ok(Map.of("attempt_id", attemptId));
    }

    @GetMapping("/exams/{examId}/questions")
    public ResponseEntity<List<QuestionResponseDTO>> getQuestions(@PathVariable Integer examId) {
        return ResponseEntity.ok(examService.getQuestionsForExam(examId));
    }

    @PostMapping("/responses")
    public ResponseEntity<Map<String, String>> saveResponse(@RequestBody ResponseRequest request) {
        studentExamService.saveResponse(request);
        return ResponseEntity.ok(Map.of("message", "Saved"));
    }

    @PostMapping("/exams/{attemptId}/submit")
    public ResponseEntity<Map<String, String>> submitExam(@PathVariable Integer attemptId) {
        studentExamService.submitExam(attemptId);
        return ResponseEntity.ok(Map.of("message", "Submitted"));
    }

    @GetMapping("/results")
    public ResponseEntity<List<Map<String, Object>>> getResults(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request) {

        Integer userId = extractUserId(authHeader, request);
        if (userId == null) {
            userId = 2;
        }

        return ResponseEntity.ok(studentExamService.getStudentResults(userId));
    }

    private Integer extractUserId(String authHeader, HttpServletRequest request) {
        Object attr = request.getAttribute("authenticated_user_id");
        if (attr instanceof Integer) {
            return (Integer) attr;
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.extractUserId(token);
            }
        }
        return null;
    }
}
