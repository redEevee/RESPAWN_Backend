package com.shop.respawn.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    RUNTIME_EXCEPTION(HttpStatus.BAD_REQUEST, "E0001"),
    ACCESS_DENIED_EXCEPTION(HttpStatus.UNAUTHORIZED, "E0002", "접근 권한이 없습니다."),
    NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "E0003"),
    PARAMETER_INCORRECT(HttpStatus.BAD_REQUEST, "E0004", "파라미터(인자)가 잘못되었습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E0100"),
    ALREADY_EXIST_USERNAME(HttpStatus.CONFLICT, "E409", "이미 존재하는 아이디입니다.");

    private final HttpStatus status;
    private final String code;
    private String message;

    ErrorCode(HttpStatus status, String code) {
        this.status = status;
        this.code = code;
    }

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}