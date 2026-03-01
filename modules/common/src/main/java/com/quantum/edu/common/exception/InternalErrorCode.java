package com.quantum.edu.common.exception;

public enum InternalErrorCode {

    // Auth
    EMAIL_ALREADY_EXISTS("QE_INT_001", "An account with this email already exists", ApiErrorCode.EMAIL_ALREADY_EXISTS),
    INVALID_CREDENTIALS("QE_INT_002", "Invalid email or password", ApiErrorCode.INVALID_CREDENTIALS),
    EMAIL_NOT_VERIFIED("QE_INT_003", "Email not verified", ApiErrorCode.EMAIL_NOT_VERIFIED),
    INVALID_VERIFICATION_TOKEN("QE_INT_004", "Invalid verification token", ApiErrorCode.INVALID_VERIFICATION_TOKEN),
    VERIFICATION_TOKEN_EXPIRED("QE_INT_005", "Verification token has expired", ApiErrorCode.VERIFICATION_TOKEN_EXPIRED),

    // User Management
    USER_MANAGEMENT_VALIDATION_FAILED("QE_INT_006", "Validation failed", ApiErrorCode.USER_MANAGEMENT_VALIDATION_FAILED),
    USER_PROFILE_ALREADY_EXISTS("QE_INT_007", "User profile already exists", ApiErrorCode.USER_PROFILE_ALREADY_EXISTS),
    USER_PROFILE_NOT_FOUND("QE_INT_028", "User profile not found", ApiErrorCode.USER_PROFILE_NOT_FOUND),

    // Product Catalogue
    CATEGORY_NOT_FOUND("QE_INT_008", "Category not found", ApiErrorCode.CATEGORY_NOT_FOUND),
    PRODUCT_NOT_FOUND("QE_INT_009", "Product not found", ApiErrorCode.PRODUCT_NOT_FOUND),
    DUPLICATE_CATEGORY_SLUG("QE_INT_010", "Category slug already exists", ApiErrorCode.DUPLICATE_CATEGORY_SLUG),
    DUPLICATE_PRODUCT_SLUG("QE_INT_011", "Product slug already exists", ApiErrorCode.DUPLICATE_PRODUCT_SLUG),
    CIRCULAR_CATEGORY_REFERENCE("QE_INT_012", "Circular category reference detected", ApiErrorCode.CIRCULAR_CATEGORY_REFERENCE),
    CONTENT_NOT_FOUND("QE_INT_013", "Product content not found", ApiErrorCode.CONTENT_NOT_FOUND),
    DUPLICATE_ORDER_INDEX("QE_INT_014", "Content order index already exists", ApiErrorCode.DUPLICATE_ORDER_INDEX),
    ASSESSMENT_NOT_FOUND("QE_INT_015", "Assessment not found", ApiErrorCode.ASSESSMENT_NOT_FOUND),
    QUESTION_NOT_FOUND("QE_INT_016", "Question not found", ApiErrorCode.QUESTION_NOT_FOUND),
    PRODUCT_CATALOGUE_VALIDATION_FAILED("QE_INT_017", "Validation failed", ApiErrorCode.PRODUCT_CATALOGUE_VALIDATION_FAILED),
    MODULE_NOT_FOUND("QE_INT_027", "Product module not found", ApiErrorCode.MODULE_NOT_FOUND),

    // BFF
    PRICE_MISMATCH("QE_INT_029", "Product price does not match. Please refresh and try again.", ApiErrorCode.PRICE_MISMATCH),

    // Cart
    CART_EMPTY("QE_INT_018", "Cart is empty", ApiErrorCode.CART_EMPTY),
    CART_ITEM_ALREADY_EXISTS("QE_INT_019", "Product already in cart", ApiErrorCode.CART_ITEM_ALREADY_EXISTS),
    CART_ALL_ITEMS_OWNED("QE_INT_020", "All items in cart are already owned by you", ApiErrorCode.CART_ALL_ITEMS_OWNED),
    CART_ITEMS_ALREADY_OWNED("QE_INT_021", "One or more products are already owned. Remove from cart before checkout", ApiErrorCode.CART_ITEMS_ALREADY_OWNED),
    CART_INVALID_WEBHOOK_SIGNATURE("QE_INT_022", "Invalid webhook signature", ApiErrorCode.CART_INVALID_WEBHOOK_SIGNATURE),
    CART_INVALID_REQUEST("QE_INT_023", "Invalid request", ApiErrorCode.CART_INVALID_REQUEST),
    CART_SINGLE_ITEM_ONLY("QE_INT_025", "Cart supports only one item. Remove the existing item before adding another.", ApiErrorCode.CART_SINGLE_ITEM_ONLY),
    CART_CHECKOUT_ITEMS_MISMATCH("QE_INT_030", "Checkout items do not match cart. Please refresh and try again.", ApiErrorCode.CART_CHECKOUT_ITEMS_MISMATCH),

    // Ownership
    OWNERSHIP_ALREADY_EXISTS("QE_INT_024", "User already owns this course", ApiErrorCode.OWNERSHIP_ALREADY_EXISTS);

    private final String code;
    private final String message;
    private final ApiErrorCode mappedApiErrorCode;

    InternalErrorCode(String code, String message, ApiErrorCode mappedApiErrorCode) {
        this.code = code;
        this.message = message;
        this.mappedApiErrorCode = mappedApiErrorCode;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public ApiErrorCode getMappedApiErrorCode() {
        return mappedApiErrorCode;
    }
}
