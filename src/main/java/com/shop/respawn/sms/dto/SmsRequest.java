package com.shop.respawn.sms.dto;

import lombok.Data;

public class SmsRequest {

    @Data
    public static class PhoneNumberForVerificationRequest{
        private String phoneNumber;
    }

    @Data
    public static class VerificationCodeRequest{
        private String code;
    }
}
