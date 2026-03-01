package com.quantum.edu.catalogue.repository;

import com.quantum.edu.catalogue.domain.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @Query("SELECT a FROM Assessment a WHERE a.productContent.id = :contentId")
    Optional<Assessment> findByProductContentId(@Param("contentId") Long contentId);

    @Query("SELECT DISTINCT a FROM Assessment a " +
            "LEFT JOIN FETCH a.questions " +
            "WHERE a.productContent.id = :contentId")
    Optional<Assessment> findByProductContentIdWithQuestionsAndOptions(@Param("contentId") Long contentId);
}
