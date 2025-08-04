package com.shop.respawn.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import static com.shop.respawn.domain.ItemStatus.*;

@Document(collection = "item")
@Getter @Setter
public class Item {

    @Id
    private String id;

    private String name;
    @Column(columnDefinition = "TEXT")
    private String deliveryType;
    private String deliveryFee;
    private String company;
    private Long companyNumber;
    private int price;
    private int stockQuantity;
    private String sellerId;
    private String imageUrl;
    private List<String> categoryIds = new ArrayList<>();
    private String description;

    private ItemStatus status = SALE;

    //==비즈니스 로직==//
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new RuntimeException("재고가 부족합니다");
        }
        this.stockQuantity = restStock;
    }
}
