package com.shop.respawn.domain;

import com.shop.respawn.domain.item.Item;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Seller {

    @Id @GeneratedValue
    @Column(name = "seller_id")
    private Long id;

    private String name;

    private String password;

    private String username;

    private String phoneNumber;

    private String email;

    @OneToMany(mappedBy = "seller")
    private List<Item> items =  new ArrayList<Item>();
}
