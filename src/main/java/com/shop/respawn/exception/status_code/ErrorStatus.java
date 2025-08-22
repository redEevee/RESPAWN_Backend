package com.shop.respawn.exception.status_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode{

    // email verification
    _VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION4001", "만료된 인증 코드입니다."),
    _VERIFICATION_CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION4002", "존재하지 않는 인증 코드입니다."),
    _PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
