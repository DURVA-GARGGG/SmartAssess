package com.smartexam.repository;

import com.smartexam.entity.StudentExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentExamRepository extends JpaRepository<StudentExam, Integer> {
    Optional<StudentExam> findByUserUserIdAndExamExamId(Integer userId, Integer examId);
    List<StudentExam> findByUserUserId(Integer userId);
    List<StudentExam> findByExamExamId(Integer examId);
}
