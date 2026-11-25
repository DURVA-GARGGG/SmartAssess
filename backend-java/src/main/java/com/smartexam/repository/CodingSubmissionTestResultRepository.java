package com.smartexam.repository;

import com.smartexam.entity.CodingSubmissionTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodingSubmissionTestResultRepository extends JpaRepository<CodingSubmissionTestResult, Integer> {
    List<CodingSubmissionTestResult> findBySubmissionSubmissionId(Integer submissionId);
}
