package com.shop.respawn.dto.adminMemo;

import com.shop.respawn.domain.AdminMemo;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminMemoResponse {
    private String id;
    private String userType;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminMemoResponse from(AdminMemo adminMemo) {
        return AdminMemoResponse.builder()
                .id(adminMemo.getId())
                .userType(adminMemo.getUserType())
                .userId(adminMemo.getUserId())
                .content(adminMemo.getContent())
                .createdAt(adminMemo.getCreatedAt())
                .updatedAt(adminMemo.getUpdatedAt())
                .build();
    }
}
