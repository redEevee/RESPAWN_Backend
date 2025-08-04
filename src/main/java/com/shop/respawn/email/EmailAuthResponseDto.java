package com.shop.respawn.email;

import lombok.Data;

@Data
public class EmailAuthResponseDto {
    private boolean success;
    private String responseMessage;

    public EmailAuthResponseDto(boolean success, String responseMessage){
        this.success = success;
        this.responseMessage = responseMessage;
    }
}
