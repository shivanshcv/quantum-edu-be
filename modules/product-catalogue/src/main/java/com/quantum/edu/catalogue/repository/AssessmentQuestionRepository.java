package com.quantum.edu.catalogue.repository;

import com.quantum.edu.catalogue.domain.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {

    List<AssessmentQuestion> findByAssessmentId(Long assessmentId);
}
