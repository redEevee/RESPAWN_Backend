package com.shop.respawn.dto.findInfo;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String currentPassword;
    private String newPassword;
}
