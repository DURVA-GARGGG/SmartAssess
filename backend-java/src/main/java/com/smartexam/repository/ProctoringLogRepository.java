package com.smartexam.repository;

import com.smartexam.entity.ProctoringLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProctoringLogRepository extends JpaRepository<ProctoringLog, Integer> {
    Optional<ProctoringLog> findByStudentExamAttemptId(Integer attemptId);
}
