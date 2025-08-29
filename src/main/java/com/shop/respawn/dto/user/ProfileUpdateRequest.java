package com.shop.respawn.dto.user;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String name;
    private String phoneNumber;
    private String email;
}
