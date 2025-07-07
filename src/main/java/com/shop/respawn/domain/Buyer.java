package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.*;

@Entity
@Getter @Setter
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

    @OneToMany(mappedBy = "buyer")
    private List<Address> addresses = new ArrayList<Address>();

    @OneToMany(mappedBy = "buyer")
    private List<Cart> cart = new ArrayList<Cart>();

    @OneToMany(mappedBy = "buyer")
    private List<Order> orders = new ArrayList<Order>();

}
