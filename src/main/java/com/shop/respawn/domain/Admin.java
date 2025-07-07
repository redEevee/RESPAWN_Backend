package com.shop.respawn.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Admin {

    @Id @GeneratedValue
    private Long id;

    private String name;
    private String username;
    private String password;
    private String phoneNumber;
    private String email;
}
