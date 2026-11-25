package com.smartexam.service;

import com.smartexam.dto.ExamRequest;
import com.smartexam.dto.QuestionRequest;
import com.smartexam.dto.QuestionResponseDTO;
import com.smartexam.entity.Exam;

import java.util.List;

public interface ExamService {
    List<Exam> getAllExams();
    Exam getExamById(Integer examId);
    Exam createExam(ExamRequest request);
    List<QuestionResponseDTO> getQuestionsForExam(Integer examId);
    void addQuestionToExam(Integer examId, QuestionRequest request);
}
