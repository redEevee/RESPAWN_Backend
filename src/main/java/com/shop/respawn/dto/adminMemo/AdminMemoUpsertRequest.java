package com.shop.respawn.dto.adminMemo;

import lombok.Data;

@Data
public class AdminMemoUpsertRequest {
    private String userType;  // "buyer" | "seller"
    private Long userId;
    private String content;     // 메모 내용
}