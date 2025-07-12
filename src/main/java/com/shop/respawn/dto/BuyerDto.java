package com.shop.respawn.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BuyerDto {

    private String name;

    private String username;

    private String password;

    private String email;

    private String phoneNumber;

    public BuyerDto(String name, String username, String password, String email, String phoneNumber) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public BuyerDto(String name, String username, String password, String email) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
