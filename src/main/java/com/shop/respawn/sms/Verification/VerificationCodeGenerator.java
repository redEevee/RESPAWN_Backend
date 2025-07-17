package com.shop.respawn.sms.Verification;

import java.time.LocalDateTime;
import java.util.Random;

public class VerificationCodeGenerator {
    private static final Integer EXPIRATION_TIME_IN_MINUTES = 5;

    public static VerificationCode generateVerificationCode(LocalDateTime sentAt) {

        Random random = new Random();
        int randomInt = random.nextInt(900_000) + 100_000;
        String code = String.valueOf(randomInt);
        System.out.println("code = " + code);

        return VerificationCode.builder()
                .code(code)
                .createAt(sentAt)
                .expirationTimeInMinutes(EXPIRATION_TIME_IN_MINUTES)
                .build();
    }
}