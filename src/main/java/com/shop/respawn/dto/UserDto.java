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

    private String company;

    private Long companyNumber;

    private String email;

    private String phoneNumber;

    private String provider;

    private String providerId;

    private Role role;

    public UserDto(String name, String username, String email, String phoneNumber, Role role) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

}
