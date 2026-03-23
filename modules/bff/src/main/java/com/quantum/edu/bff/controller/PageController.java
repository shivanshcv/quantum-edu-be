package com.quantum.edu.bff.controller;

import com.quantum.edu.bff.dto.BffVerifyCartRequest;
import com.quantum.edu.bff.dto.MyLearningPageResponse;
import com.quantum.edu.bff.dto.PageResponse;
import com.quantum.edu.bff.service.CoursesPageService;
import com.quantum.edu.bff.service.HomePageService;
import com.quantum.edu.bff.service.PDPPageService;
import com.quantum.edu.bff.service.CartPageService;
import com.quantum.edu.bff.service.MyLearningPageService;
import com.quantum.edu.bff.service.SettingsPageService;
import com.quantum.edu.bff.service.VerifyCartPageService;
import com.quantum.edu.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pages")
public class PageController {

    private final HomePageService homePageService;
    private final CoursesPageService coursesPageService;
    private final PDPPageService pdpPageService;
    private final CartPageService cartPageService;
    private final MyLearningPageService myLearningPageService;
    private final VerifyCartPageService verifyCartPageService;
    private final SettingsPageService settingsPageService;

    public PageController(HomePageService homePageService,
                          CoursesPageService coursesPageService,
                          PDPPageService pdpPageService,
                          CartPageService cartPageService,
                          MyLearningPageService myLearningPageService,
                          VerifyCartPageService verifyCartPageService,
                          SettingsPageService settingsPageService) {
        this.homePageService = homePageService;
        this.coursesPageService = coursesPageService;
        this.pdpPageService = pdpPageService;
        this.cartPageService = cartPageService;
        this.myLearningPageService = myLearningPageService;
        this.verifyCartPageService = verifyCartPageService;
        this.settingsPageService = settingsPageService;
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<PageResponse>> settingsPage(
            @RequestAttribute("userId") Long userId) {
        PageResponse response = settingsPageService.getSettingsPage(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<PageResponse>> homePage() {
        PageResponse response = homePageService.getHomePage();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<PageResponse>> coursesPage(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        PageResponse response = coursesPageService.getCoursesPage(categoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/course")
    public ResponseEntity<ApiResponse<PageResponse>> courseDetailPage(
            @RequestParam Long productId) {
        PageResponse response = pdpPageService.getProductDetailPage(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-learning")
    public ResponseEntity<ApiResponse<MyLearningPageResponse>> myLearningPage(
            @RequestAttribute("userId") Long userId) {
        MyLearningPageResponse response = myLearningPageService.getMyLearningPage(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/cart")
    public ResponseEntity<ApiResponse<PageResponse>> cartPage(
            @RequestAttribute("userId") Long userId) {
        PageResponse response = cartPageService.getCartPage(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify-cart")
    public ResponseEntity<ApiResponse<PageResponse>> verifyCartPage(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody BffVerifyCartRequest request) {
        PageResponse response = verifyCartPageService.getVerifyCartPage(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
