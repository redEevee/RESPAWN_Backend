package com.shop.respawn.dto.findInfo;

import lombok.Data;

@Data
public class FindInfoResponse {

    private String maskedUsername;
    private String maskedEmail;
    private String maskedPhoneNumber;
    private String token;
    private Long userId;
    private String error; // 추가

    public FindInfoResponse(String maskedUsername, String maskedEmail, String maskedPhoneNumber, String token, Long userId) {
        this.maskedUsername = maskedUsername;
        this.maskedEmail = maskedEmail;
        this.maskedPhoneNumber = maskedPhoneNumber;
        this.token = token;
        this.userId = userId;
    }

    public FindInfoResponse(String error) { // 에러 전용 생성자
        this.error = error;
    }

    public FindInfoResponse(String maskedEmail, String maskedPhoneNumber, Long userId) {
        this.maskedEmail = maskedEmail;
        this.maskedPhoneNumber = maskedPhoneNumber;
        this.userId = userId;
    }
}
