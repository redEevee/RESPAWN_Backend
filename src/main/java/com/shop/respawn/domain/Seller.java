package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private String company;

    private Long companyNumber;

    private String password;

    private String email;

    private String phoneNumber;

    @Enumerated(STRING)
    private Role role;

    private Seller(String name, String username, String company, Long companyNumber,String password, String email, String phoneNumber, Role role) {
        this.name = name;
        this.username = username;
        this.company = company;
        this.companyNumber = companyNumber;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    //정적 팩토리 메서드
    public static Seller createSeller(String name, String username, String company, Long companyNumber, String password, String email, String phoneNumber, Role role) {
        Seller seller = new Seller();
        seller.name = name;
        seller.username = username;
        seller.company = company;
        seller.companyNumber = companyNumber;
        seller.password = password;
        seller.email = email;
        seller.phoneNumber = phoneNumber;
        seller.role = role;
        return seller;
    }

    public void updatePhoneNumber(String newPhoneNumber) {
        this.phoneNumber = newPhoneNumber;
    }
}
