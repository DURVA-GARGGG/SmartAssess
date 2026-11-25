package com.smartexam.repository;

import com.smartexam.entity.CodingSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodingSubmissionRepository extends JpaRepository<CodingSubmission, Integer> {
    List<CodingSubmission> findByStudentExamAttemptId(Integer attemptId);
    List<CodingSubmission> findByStudentExamAttemptIdAndQuestionQuestionId(Integer attemptId, Integer questionId);
    Optional<CodingSubmission> findTopByStudentExamAttemptIdAndQuestionQuestionIdOrderByScoreObtainedDescSubmittedAtDesc(
            Integer attemptId, Integer questionId);
}
