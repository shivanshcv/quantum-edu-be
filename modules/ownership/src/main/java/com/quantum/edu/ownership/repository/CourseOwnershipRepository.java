package com.quantum.edu.ownership.repository;

import com.quantum.edu.ownership.domain.CourseOwnership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseOwnershipRepository extends JpaRepository<CourseOwnership, Long> {

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);

    Optional<CourseOwnership> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseOwnership> findByUserIdOrderByPurchasedAtDesc(Long userId);
}
