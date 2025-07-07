package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.FetchType.*;
import static lombok.AccessLevel.*;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter @Setter
public class Address {

    @Id @GeneratedValue
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    private String addressName;
    private String city;
    private String street;
    private String zipcode;

    public Address(String addressName, String city, String street, String zipcode) {
        this.addressName = addressName;
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
