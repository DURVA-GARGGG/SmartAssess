package com.smartexam.controller;

import com.smartexam.dto.ExamRequest;
import com.smartexam.dto.QuestionRequest;
import com.smartexam.entity.Exam;
import com.smartexam.service.AdminService;
import com.smartexam.service.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final ExamService examService;
    private final AdminService adminService;

    public AdminController(ExamService examService, AdminService adminService) {
        this.examService = examService;
        this.adminService = adminService;
    }

    @GetMapping("/exams")
    public ResponseEntity<List<Exam>> getExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @PostMapping("/exams")
    public ResponseEntity<Map<String, String>> createExam(@RequestBody ExamRequest request) {
        examService.createExam(request);
        return ResponseEntity.ok(Map.of("message", "Exam created successfully"));
    }

    @PostMapping("/exams/{examId}/questions")
    public ResponseEntity<Map<String, String>> addQuestion(
            @PathVariable Integer examId,
            @RequestBody QuestionRequest request) {
        examService.addQuestionToExam(examId, request);
        return ResponseEntity.ok(Map.of("message", "Question added successfully"));
    }

    @GetMapping("/results")
    public ResponseEntity<List<Map<String, Object>>> getResults() {
        return ResponseEntity.ok(adminService.getAllResults());
    }
}
