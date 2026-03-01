package com.quantum.edu.lms.repository;

import com.quantum.edu.lms.domain.UserAssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAssessmentResultRepository extends JpaRepository<UserAssessmentResult, Long> {

    Optional<UserAssessmentResult> findByUserIdAndProductContentId(Long userId, Long productContentId);
}
