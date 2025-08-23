package com.shop.respawn.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.LocalDateTime;

@Embeddable
@Data
public class AccountStatus {

    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    // 계정 만료일 (null이면 무기한)
    private LocalDateTime accountExpiryDate;

    // 로그인 실패 횟수 추가
    private int failedLoginAttempts = 0;

    // 마지막 비밀번호 변경 시각(신규 추가)
    private LocalDateTime lastPasswordChangedAt;

    //기본 생성자
    public AccountStatus() {}

    public void increaseFailedLoginAttempts() {
        if (accountNonExpired) {
            if (this.failedLoginAttempts < 5) { // 상한 5
                this.failedLoginAttempts++;
                if (this.failedLoginAttempts >= 5) {
                    this.accountNonLocked = false;
                }
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
        // 최초 가입 시 비밀번호 변경 기준 시각 초기화
        if (this.lastPasswordChangedAt == null) {
            this.lastPasswordChangedAt = LocalDateTime.now();
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

    // 3개월(약 90일) 경과 여부 체크
    public boolean isPasswordChangeDue(int months) {
        if (lastPasswordChangedAt == null) return true; // 기록 없으면 변경 유도
        return lastPasswordChangedAt.plusMonths(months).isBefore(LocalDateTime.now())
                || lastPasswordChangedAt.plusMonths(months).isEqual(LocalDateTime.now());
    }

    // 비밀번호 변경 시각 갱신
    public void markPasswordChangedNow() {
        this.lastPasswordChangedAt = LocalDateTime.now();
        // 선택 사항: 동시에 passwordExpiryDate(정책상 강제 만료일)를 재설정하고 싶다면 아래 사용
        // this.passwordExpiryDate = LocalDateTime.now().plusMonths(3);
    }

}
