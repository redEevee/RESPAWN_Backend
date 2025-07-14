package com.shop.respawn.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode error;

    public ApiException(ErrorCode e) {
        super(e.getMessage());
        this.error = e;
    }

    public ErrorCode getErrorCode() {
        return error;
    }

}
