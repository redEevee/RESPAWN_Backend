package com.shop.respawn.dto.user;

import com.shop.respawn.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginOkResponse {
    private String name;
    private String username;
    private String authorities;           // 예: "[ROLE_USER]"
    private Role role;
    private boolean passwordChangeDue;
    private boolean passwordChangeSnoozed;
    private Long userId;                  // 세션 저장용
}
