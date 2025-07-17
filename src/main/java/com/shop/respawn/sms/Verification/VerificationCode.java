package com.shop.respawn.sms.Verification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VerificationCode {
    private String code;
    private LocalDateTime createAt;
    private Integer expirationTimeInMinutes;

    public boolean isExpired(LocalDateTime verifiedAt) {
        LocalDateTime expiredAt = createAt.plusMinutes(expirationTimeInMinutes);
        return verifiedAt.isAfter(expiredAt);
    }

    public String generateCodeMessage() {
        String formattedExpiredAt = createAt
                .plusMinutes(expirationTimeInMinutes)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return String.format(
                """
                [RESPAWN] 본인확인 인증번호 안내
                
                인증번호: %s
                
                ※ 타인에게 절대 공유하지 마세요.
                만료시각: %s
                """,
                code, formattedExpiredAt
        );
    }
}