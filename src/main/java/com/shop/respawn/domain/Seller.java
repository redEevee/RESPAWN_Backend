package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Seller {

    @Id @GeneratedValue
    @Column(name = "seller_id")
    private Long id;

    private String name;

    @Column(unique = true)
    private String username;

    private String password;

    private String email;

    private String phoneNumber;

    private String provider;

    private String providerId;

    @Enumerated(STRING)
    private Role role;

    private Seller(String name, String username, String password, String email, String phoneNumber, String provider, String providerId, Role role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }

    //정적 팩토리 메서드
    public static Seller createSeller(String username, Role role) {
        Seller seller = new Seller();
        seller.username = username;
        seller.role = role;
        return seller;
    }

    public static Seller createSeller(String name, String username, String password, String email, String phoneNumber, Role role) {
        Seller seller = new Seller();
        seller.name = name;
        seller.username = username;
        seller.password = password;
        seller.email = email;
        seller.phoneNumber = phoneNumber;
        seller.role = role;
        return seller;
    }

    public static Seller createSeller(String name, String username, String password, String email, String phoneNumber,
                                    Role role,  String provider, String providerId) {
        Seller seller = createSeller(name, username, password, email, phoneNumber, role);
        seller.provider = provider;
        seller.providerId = providerId;
        return seller;
    }

    public Seller(String name, String username, String password, String email, String phoneNumber, Role role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public void updatePhoneNumber(String newPhoneNumber) {
        this.phoneNumber = newPhoneNumber;
    }
}
