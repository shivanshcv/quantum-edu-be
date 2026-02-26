package com.quantum.edu.ownership.controller;

import com.quantum.edu.common.dto.ApiResponse;
import com.quantum.edu.ownership.dto.OwnedCourseResponse;
import com.quantum.edu.ownership.dto.OwnedCoursesResponse;
import com.quantum.edu.ownership.dto.OwnershipCheckResponse;
import com.quantum.edu.ownership.repository.CourseOwnershipRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ownership")
public class OwnershipController {

    private final CourseOwnershipRepository repository;

    public OwnershipController(CourseOwnershipRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<OwnedCoursesResponse>> getOwnedCourses(
            @RequestAttribute("userId") Long userId) {
        List<OwnedCourseResponse> courses = repository.findByUserIdOrderByPurchasedAtDesc(userId)
                .stream()
                .map(o -> OwnedCourseResponse.builder()
                        .productId(o.getCourseId())
                        .purchasedAt(o.getPurchasedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(OwnedCoursesResponse.builder().courses(courses).build()));
    }

    @GetMapping("/owns/{productId}")
    public ResponseEntity<ApiResponse<OwnershipCheckResponse>> checkOwnership(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long productId) {
        boolean owns = repository.existsByUserIdAndCourseId(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(OwnershipCheckResponse.builder().owns(owns).build()));
    }
}
