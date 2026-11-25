package com.smartexam.repository;

import com.smartexam.entity.CodingTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodingTemplateRepository extends JpaRepository<CodingTemplate, Integer> {
    List<CodingTemplate> findByQuestionQuestionId(Integer questionId);
    Optional<CodingTemplate> findByQuestionQuestionIdAndLanguage(Integer questionId, String language);
}
