package com.quantum.edu.common.exception;

public enum ApiErrorCode {

    // Global
    INTERNAL_SERVER_ERROR("QE_001", "Internal server error", 500),
    VALIDATION_FAILED("QE_VAL_001", "Validation failed", 400),
    INVALID_REQUEST_BODY("QE_VAL_002", "Invalid request body", 400),

    // Auth
    UNAUTHORIZED("QE_AUTH_001", "Invalid or missing authentication token", 401),
    EMAIL_ALREADY_EXISTS("QE_AUTH_002", "An account with this email already exists", 409),
    INVALID_CREDENTIALS("QE_AUTH_003", "Invalid email or password", 401),
    EMAIL_NOT_VERIFIED("QE_AUTH_004", "Please verify your email before logging in", 403),
    INVALID_VERIFICATION_TOKEN("QE_AUTH_005", "Invalid verification token", 400),
    VERIFICATION_TOKEN_EXPIRED("QE_AUTH_006", "Verification token has expired or already been used", 404),

    // User Management
    USER_MANAGEMENT_VALIDATION_FAILED("QE_UM_001", "Validation failed", 400),
    USER_PROFILE_ALREADY_EXISTS("QE_UM_002", "User profile already exists for this user", 409),
    USER_PROFILE_NOT_FOUND("QE_UM_003", "User profile not found", 404),

    // Product Catalogue
    CATEGORY_NOT_FOUND("QE_PC_001", "Category not found", 404),
    PRODUCT_NOT_FOUND("QE_PC_002", "Product not found", 404),
    DUPLICATE_CATEGORY_SLUG("QE_PC_003", "Category slug already exists", 409),
    DUPLICATE_PRODUCT_SLUG("QE_PC_004", "Product slug already exists", 409),
    CIRCULAR_CATEGORY_REFERENCE("QE_PC_005", "Circular category reference detected", 400),
    CONTENT_NOT_FOUND("QE_PC_006", "Product content not found", 404),
    DUPLICATE_ORDER_INDEX("QE_PC_007", "Content order index already exists", 409),
    ASSESSMENT_NOT_FOUND("QE_PC_008", "Assessment not found", 404),
    QUESTION_NOT_FOUND("QE_PC_009", "Question not found", 404),
    PRODUCT_CATALOGUE_VALIDATION_FAILED("QE_PC_010", "Validation failed", 400),
    MODULE_NOT_FOUND("QE_PC_012", "Product module not found", 404),

    // BFF
    PRICE_MISMATCH("QE_BFF_001", "Product price does not match. Please refresh and try again.", 400),

    // Cart
    CART_EMPTY("QE_CART_004", "Cart is empty", 400),
    CART_ITEM_ALREADY_EXISTS("QE_CART_003", "Product already in cart", 409),
    CART_ALL_ITEMS_OWNED("QE_CART_006", "All items in cart are already owned by you", 400),
    CART_ITEMS_ALREADY_OWNED("QE_CART_007", "One or more products are already owned. Remove from cart before checkout", 400),
    CART_INVALID_WEBHOOK_SIGNATURE("QE_CART_005", "Invalid webhook signature", 400),
    CART_INVALID_REQUEST("QE_CART_001", "Invalid request", 400),
    CART_SINGLE_ITEM_ONLY("QE_CART_008", "Cart supports only one item. Remove the existing item before adding another.", 400),
    CART_CHECKOUT_ITEMS_MISMATCH("QE_CART_009", "Checkout items do not match cart. Please refresh and try again.", 400),

    // Ownership
    OWNERSHIP_ALREADY_EXISTS("QE_OWN_001", "User already owns this course", 409),

    // LMS
    LMS_COURSE_ACCESS_DENIED("QE_LMS_001", "Course access denied", 403),
    LMS_CONTENT_NOT_FOUND("QE_LMS_002", "Lesson not found", 404),
    LMS_QUIZ_NOT_PASSED("QE_LMS_003", "You must pass the quiz before completing this lesson", 400);

    private final String code;
    private final String message;
    private final int httpStatus;

    ApiErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
