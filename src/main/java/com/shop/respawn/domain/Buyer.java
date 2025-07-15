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

    @Column(unique = true)
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

    private Buyer(String name, String username, String password, String email, String phoneNumber, String provider, String providerId, Role role) {
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
    public static Buyer createBuyer(String username, Role role) {
        Buyer buyer = new Buyer();
        buyer.username = username;
        buyer.role = role;
        return buyer;
    }

    public static Buyer createBuyer(String name, String username, String password, String email, String phoneNumber, Role role) {
        Buyer buyer = new Buyer();
        buyer.name = name;
        buyer.username = username;
        buyer.password = password;
        buyer.email = email;
        buyer.phoneNumber = phoneNumber;
        buyer.role = role;
        return buyer;
    }

    public static Buyer createBuyer(String name, String username, String password, String email, String phoneNumber,
                                    Role role,  String provider, String providerId) {
        Buyer buyer = createBuyer(name, username, password, email, phoneNumber, role);
        buyer.provider = provider;
        buyer.providerId = providerId;
        return buyer;
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.setBuyer(this);
    }

    public Buyer(String name, String username, String password, String email, String phoneNumber, Role role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}
