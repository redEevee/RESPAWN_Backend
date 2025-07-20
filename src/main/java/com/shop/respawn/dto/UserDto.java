package com.shop.respawn.dto;

import com.shop.respawn.domain.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {

    private String userType;

    private String name;

    private String username;

    private String password;

    private String email;

    private String phoneNumber;

    private String provider;

    private String providerId;

    private Role role;

    public UserDto(String name, String username, String password, String email, String phoneNumber, String provider, String providerId, Role role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }
}
