package com.shop.respawn.exception.status_code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

//전화번호 인증에 대한 Status 타입
@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode{

    _OK(HttpStatus.OK, "COMMON200", "성공입니다."),
    _NO_CONTENT(HttpStatus.NO_CONTENT, "COMMON204", "별도의 응답 데이터가 없으며, 정상 처리되었습니다."),

    _PASSWORD_CHECKED(HttpStatus.OK, "PASSWORD_CHECKED", "비밀번호가 확인 됐습니다."),
    _PASSWORD_CHANGED(HttpStatus.OK, "PASSWORD_CHANGED", "비밀번호가 성공적으로 변경되었습니다."),
    _PHONE_NUMBER_CHANGED(HttpStatus.OK, "PHONE-NUMBER_CHANGED", "전화번호가 성공적으로 변경되었습니다.");

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
