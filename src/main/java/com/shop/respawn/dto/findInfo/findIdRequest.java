package com.shop.respawn.dto.findInfo;

import lombok.Data;

@Data
public class findIdRequest {

    private String userType;
    private String name;
    private String email;
    private String phoneNumber;

    private String token;
    private String type;
    private Long userId;

}
