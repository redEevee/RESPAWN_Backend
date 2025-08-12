package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id @GeneratedValue
    @Column(name = "admin_id")
    private Long id;

    private String name;
    @Column(unique = true)
    private String username;
    private String password;
    @Enumerated(STRING)
    private Role role;

    //정적 팩토리 메서드
    public static Admin createAdmin(String name, String username, String password, Role role) {
        return Admin.builder()
                .name(name)
                .username(username)
                .password(password)
                .role(role)
                .build();
    }
}
