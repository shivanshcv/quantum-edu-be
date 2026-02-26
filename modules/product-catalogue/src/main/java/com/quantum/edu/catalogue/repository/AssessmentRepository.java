package com.quantum.edu.catalogue.repository;

import com.quantum.edu.catalogue.domain.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    Optional<Assessment> findByProductContentId(Long productContentId);
}
