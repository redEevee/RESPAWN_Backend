package com.shop.respawn.exception;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private static final String SUCCESS_STATUS = "success";
    private static final String FAIL_STATUS = "fail";

    private String status;
    private T data;
    private ErrorMessage errorMsg;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_STATUS, data, null);
    }

    public static ApiResponse<?> successWithNoContent() {
        return new ApiResponse<>(SUCCESS_STATUS, null, null);
    }

    public static ApiResponse<?> fail(ErrorMessage errorMessage) {
        return new ApiResponse<>(FAIL_STATUS, null, errorMessage);
    }

    protected ApiResponse() {
    }

    private ApiResponse(String status, T data, ErrorMessage errorMessage) {
        this.status = status;
        this.data = data;
        this.errorMsg = errorMessage;
    }
}