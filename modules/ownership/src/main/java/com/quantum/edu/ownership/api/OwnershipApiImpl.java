package com.quantum.edu.ownership.api;

import com.quantum.edu.common.exception.InternalErrorCode;
import com.quantum.edu.common.exception.InternalException;
import com.quantum.edu.ownership.domain.CourseOwnership;
import com.quantum.edu.ownership.repository.CourseOwnershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OwnershipApiImpl implements OwnershipApi {

    private final CourseOwnershipRepository repository;

    public OwnershipApiImpl(CourseOwnershipRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean ownsCourse(Long userId, Long productId) {
        return repository.existsByUserIdAndCourseId(userId, productId);
    }

    @Override
    @Transactional
    public void createOwnership(Long userId, Long courseId, Long orderId) {
        if (repository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new InternalException(InternalErrorCode.OWNERSHIP_ALREADY_EXISTS);
        }
        CourseOwnership ownership = new CourseOwnership(userId, courseId, orderId, Instant.now());
        repository.save(ownership);
    }
}
