package com.shop.respawn.dto.user;

import com.shop.respawn.domain.AccountStatus;
import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.domain.Seller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private String userType;        // "buyer", "seller"
    private Long id;
    private String name;
    private String username;
    private String company;
    private Long companyNumber;
    private String email;
    private String phoneNumber;
    private Role role;              // ROLE_USER / ROLE_SELLER
    private LocalDateTime createdAt; // 가입일

    // AccountStatus 주요 필드
    private Boolean accountNonExpired;   // 만료 아님
    private Boolean accountNonLocked;    // 잠김 아님
    private Boolean credentialsNonExpired;
    private Boolean enabled;             // 사용 가능 여부
    private LocalDateTime accountExpiryDate;
    private Integer failedLoginAttempts;
    private LocalDateTime lastPasswordChangedAt;

    // --- 공통 상태 매핑 헬퍼 ---
    private static void applyStatus(UserSummaryDtoBuilder userSummaryDtoBuilder, AccountStatus accountStatus) {
        if (accountStatus == null) {
            userSummaryDtoBuilder
                    .accountNonExpired(null)
                    .accountNonLocked(null)
                    .credentialsNonExpired(null)
                    .enabled(null)
                    .accountExpiryDate(null)
                    .failedLoginAttempts(null)
                    .lastPasswordChangedAt(null);
            return;
        }
        userSummaryDtoBuilder
                .accountNonExpired(accountStatus.isAccountNonExpired())
                .accountNonLocked(accountStatus.isAccountNonLocked())
                .credentialsNonExpired(accountStatus.isCredentialsNonExpired())
                .enabled(accountStatus.isEnabled())
                .accountExpiryDate(accountStatus.getAccountExpiryDate())
                .failedLoginAttempts(accountStatus.getFailedLoginAttempts())
                .lastPasswordChangedAt(accountStatus.getLastPasswordChangedAt());
    }

    // --- Seller 변환 ---
    public static UserSummaryDto fromSeller(Seller seller) {
        UserSummaryDtoBuilder userSummaryDtoBuilder = UserSummaryDto.builder()
                .userType("seller")
                .id(seller.getId())
                .name(seller.getName())
                .username(seller.getUsername())
                .company(seller.getCompany())
                .companyNumber(seller.getCompanyNumber())
                .email(seller.getEmail())
                .phoneNumber(seller.getPhoneNumber())
                .role(seller.getRole())
                .createdAt(seller.getCreatedAt());
        applyStatus(userSummaryDtoBuilder, seller.getAccountStatus());
        return userSummaryDtoBuilder.build();
    }

    // --- Buyer 변환(참고용) ---
    public static UserSummaryDto fromBuyer(Buyer buyer) {
        UserSummaryDtoBuilder userSummaryDtoBuilder = UserSummaryDto.builder()
                .userType("buyer")
                .id(buyer.getId())
                .name(buyer.getName())
                .username(buyer.getUsername())
                .email(buyer.getEmail())
                .phoneNumber(buyer.getPhoneNumber())
                .role(buyer.getRole())
                .createdAt(buyer.getCreatedAt());
        applyStatus(userSummaryDtoBuilder, buyer.getAccountStatus());
        return userSummaryDtoBuilder.build();
    }
}
