package com.smartexam.service;

import com.smartexam.dto.ResponseRequest;

import java.util.List;
import java.util.Map;

public interface StudentExamService {
    Integer startExam(Integer userId, Integer examId);
    void saveResponse(ResponseRequest request);
    void submitExam(Integer attemptId);
    List<Map<String, Object>> getStudentResults(Integer userId);
}
