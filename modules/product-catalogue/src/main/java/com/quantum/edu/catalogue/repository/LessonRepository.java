package com.quantum.edu.catalogue.repository;

import com.quantum.edu.catalogue.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    Optional<Lesson> findByProductContentId(Long productContentId);
}
