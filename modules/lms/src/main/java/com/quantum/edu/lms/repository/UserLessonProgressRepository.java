package com.quantum.edu.lms.repository;

import com.quantum.edu.lms.domain.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, Long> {

    List<UserLessonProgress> findByUserIdAndProductContentIdIn(Long userId, List<Long> productContentIds);

    Optional<UserLessonProgress> findByUserIdAndProductContentId(Long userId, Long productContentId);
}
