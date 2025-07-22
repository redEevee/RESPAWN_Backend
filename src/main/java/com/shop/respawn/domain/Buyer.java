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

    @Builder.Default
    @OneToMany(mappedBy = "buyer")
    private List<Address> addresses = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "buyer")
    private List<Cart> cart = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "buyer")
    private List<Order> orders = new ArrayList<>();

    private Buyer(String name, String username, String password, String email, String phoneNumber, Role role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    //정적 팩토리 메서드
    public static Buyer createBuyer(String name, String username, String password, String email, String phoneNumber, Role role) {
        return new Buyer(name, username, password, email, phoneNumber, role);
    }

    public void updatePhoneNumber(String newPhoneNumber) {
        this.phoneNumber = newPhoneNumber;
    }

    // 연관관계 편의 메서드
    /**
     * 주소를 추가하는 편의 메서드
     * 양방향 연관관계를 안전하게 설정합니다
     */
    public void addAddress(Address address) {
        this.addresses.add(address);
        address.setBuyer(this); // 패키지 레벨 세터 사용
    }
}
