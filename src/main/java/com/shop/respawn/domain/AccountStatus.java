package com.shop.respawn.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Getter @Setter
public class AccountStatus {

    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    // 계정 만료일 (null이면 무기한)
    private LocalDateTime accountExpiryDate;

    // 비밀번호 만료일
    private LocalDateTime passwordExpiryDate;

    // 로그인 실패 횟수 추가
    private int failedLoginAttempts = 0;

    //기본 생성자
    public AccountStatus() {}

    public void increaseFailedLoginAttempts() {
        if(accountNonExpired) {
            this.failedLoginAttempts++;
            System.out.println("failedLoginAttempts = " + failedLoginAttempts);
            if (this.failedLoginAttempts >= 5) {
                this.accountNonLocked = false; // 계정 잠금
            }
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountNonLocked = true; // 잠금 해제
    }

    // 만료일 자동 설정 생성자
    public AccountStatus(boolean useExpiryDate) {
        if (useExpiryDate) {
            this.accountExpiryDate = LocalDateTime.now().plusYears(1);
        }
    }

    public boolean isAccountNonExpired() {
        boolean notExpired = accountNonExpired &&
                (accountExpiryDate == null || accountExpiryDate.isAfter(LocalDateTime.now()));
        // 만료 상태 반영
        if (!notExpired) {
            this.accountNonExpired = false; // 만료 필드 업데이트
        }
        return notExpired;
    }

    // 날짜 비교
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired &&
                (passwordExpiryDate == null || passwordExpiryDate.isAfter(LocalDateTime.now()));
    }

}
