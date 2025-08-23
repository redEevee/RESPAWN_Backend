package com.shop.respawn.dto.findInfo;

import lombok.Data;

@Data
public class FindInfoRequest {

    private String userType;
    private String name;
    private String email;
    private String phoneNumber;
    private String username;

    private String token;
    private String type;
    private Long userId;

}
