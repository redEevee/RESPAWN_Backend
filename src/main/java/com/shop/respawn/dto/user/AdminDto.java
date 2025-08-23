package com.shop.respawn.dto.user;

import com.shop.respawn.domain.Role;
import lombok.Data;

@Data
public class AdminDto {

    private String name;

    private String username;

    private String password;

    private Role role;

    public AdminDto(String name, String username, String password, Role role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
