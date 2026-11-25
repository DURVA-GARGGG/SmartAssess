package com.smartexam.repository;

import com.smartexam.entity.StudentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentResponseRepository extends JpaRepository<StudentResponse, Integer> {
    List<StudentResponse> findByStudentExamAttemptId(Integer attemptId);
    Optional<StudentResponse> findByStudentExamAttemptIdAndQuestionQuestionId(Integer attemptId, Integer questionId);
}
