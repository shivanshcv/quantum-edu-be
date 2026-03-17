package com.quantum.edu.usermgmt.controller;

import com.quantum.edu.common.dto.ApiResponse;
import com.quantum.edu.usermgmt.api.UserProfileApi;
import com.quantum.edu.usermgmt.domain.UserProfile;
import com.quantum.edu.usermgmt.dto.UpdateBillingInfoRequest;
import com.quantum.edu.usermgmt.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usermgmt")
public class UserManagementController {

    private final UserProfileApi userProfileApi;

    public UserManagementController(UserProfileApi userProfileApi) {
        this.userProfileApi = userProfileApi;
    }

    @PatchMapping("/updateProfile")
    public ResponseEntity<ApiResponse<UserProfile>> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfile profile = userProfileApi.updateProfile(
                userId,
                request.getFirstName(),
                request.getLastName(),
                request.getPhone());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PatchMapping("/updateBillingInfo")
    public ResponseEntity<ApiResponse<UserProfile>> updateBillingInfo(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateBillingInfoRequest request) {
        UserProfile profile = userProfileApi.updateBillingInfo(
                userId,
                request.getBillingName(),
                request.getBillingAddressLine1(),
                request.getBillingAddressLine2(),
                request.getBillingCity(),
                request.getBillingState(),
                request.getBillingCountry(),
                request.getBillingPostalCode(),
                request.getBillingGstNumber());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
