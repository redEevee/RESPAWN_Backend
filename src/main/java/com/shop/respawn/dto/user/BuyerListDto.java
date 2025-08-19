package com.shop.respawn.dto.user;

import com.shop.respawn.domain.Buyer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerListDto {
    private Long id;
    private String name;         // 이름
    private String username;     // 아이디
    private String phoneNumber;  // 전화번호
    private String email;        // 이메일
    private LocalDateTime createdAt; // 가입일

    public static BuyerListDto from(Buyer buyer) {
        return BuyerListDto.builder()
                .id(buyer.getId())
                .name(buyer.getName())
                .username(buyer.getUsername())
                .phoneNumber(buyer.getPhoneNumber())
                .email(buyer.getEmail())
                .createdAt(buyer.getCreatedAt())
                .build();
    }
}
