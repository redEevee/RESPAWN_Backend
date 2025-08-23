package com.shop.respawn.exception;

import com.shop.respawn.exception.status_code.BaseCode;

/**
 * 공용 응답 DTO
 * @param code    ex) "PASSWORD_CHANGED"
 * @param message 사용자 노출용 메시지(선택)
 */
public record ApiMessage(String code, String message) {

    public static ApiMessage of(String code) {
        return new ApiMessage(code, null);
    }

    public static ApiMessage of(String code, String message) {
        return new ApiMessage(code, message);
    }

    public static ApiMessage of(BaseCode code) {
        return new ApiMessage(code.getCode(), code.getMessage());
    }
}