package com.shop.respawn.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    ROLE_USER_SELLER,
    ROLE_USER_BUYER,
    ROLE_ADMIN
}