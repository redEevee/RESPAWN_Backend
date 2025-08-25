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
    private Long userId;

    // 세션 저장용
    public LoginOkResponse(String name, String username, String authorities, Role role, Long userId) {
        this.name = name;
        this.username = username;
        this.authorities = authorities;
        this.role = role;
        this.userId = userId;
    }

    public LoginOkResponse(String name, Role role, Long userId) {
        this.name = name;
        this.role = role;
        this.userId = userId;
    }
}
