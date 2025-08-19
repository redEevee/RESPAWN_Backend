package com.shop.respawn.dto.user;

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
public class SellerListDto {
    private Long id;
    private String name;         // 이름
    private String username;     // 아이디
    private String company;      // 회사명
    private String email;        // 이메일
    private LocalDateTime createdAt; // 가입일

    public static SellerListDto from(Seller seller) {
        return SellerListDto.builder()
                .id(seller.getId())
                .name(seller.getName())
                .username(seller.getUsername())
                .company(seller.getCompany())
                .email(seller.getEmail())
                .createdAt(seller.getCreatedAt())
                .build();
    }
}
