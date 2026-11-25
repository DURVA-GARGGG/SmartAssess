package com.smartexam.repository;

import com.smartexam.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Integer> {
    Optional<Result> findByStudentExamAttemptId(Integer attemptId);

    @Query("SELECT r FROM Result r JOIN r.studentExam se WHERE se.user.userId = :userId")
    List<Result> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT r FROM Result r JOIN FETCH r.studentExam se JOIN FETCH se.user u JOIN FETCH se.exam e")
    List<Result> findAllWithDetails();
}
