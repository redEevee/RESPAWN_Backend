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
                        이도은 이녀석 너의 폰은 해킹 당했다 힣힣힣힣힣힣
                        %s
                        Expired At : %s
                                """,
                code, formattedExpiredAt
        );
    }
}