package com.smartexam.repository;

import com.smartexam.entity.CodingTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodingTestCaseRepository extends JpaRepository<CodingTestCase, Integer> {
    List<CodingTestCase> findByQuestionQuestionId(Integer questionId);
    List<CodingTestCase> findByQuestionQuestionIdAndIsSampleTrue(Integer questionId);
}
