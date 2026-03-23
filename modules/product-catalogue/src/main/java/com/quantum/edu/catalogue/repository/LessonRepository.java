package com.quantum.edu.catalogue.repository;

import com.quantum.edu.catalogue.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("SELECT l FROM Lesson l WHERE l.productContent.id = :productContentId")
    Optional<Lesson> findByProductContentId(@Param("productContentId") Long productContentId);
}
