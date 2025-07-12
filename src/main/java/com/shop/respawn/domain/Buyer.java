package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Buyer {

    @Id @GeneratedValue
    @Column(name = "buyer_id")
    private Long id;

    private String name;

    private String username;

    private String password;

    private String email;

    private String phoneNumber;

    private String provider;

    private String providerId;

    @Enumerated(STRING)
    private Role role;

    @OneToMany(mappedBy = "buyer")
    private List<Address> addresses = new ArrayList<Address>();

    @OneToMany(mappedBy = "buyer")
    private List<Cart> cart = new ArrayList<Cart>();

    @OneToMany(mappedBy = "buyer")
    private List<Order> orders = new ArrayList<Order>();

    public Buyer(String name) {
        this.name = name;
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.setBuyer(this);
    }

    public Buyer(String name, String username, String password, String email, String phoneNumber) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}
