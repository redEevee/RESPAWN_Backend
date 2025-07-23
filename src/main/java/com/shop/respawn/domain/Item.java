package com.shop.respawn.domain;

import com.shop.respawn.exception.NotEnoughStockException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "item")
@Getter @Setter
public class Item {

    @Id
    private String id;

    private String name;
    private String description;
    private String deliveryType;
    private String deliveryFee;
    private String company;
    private int price;
    private int stockQuantity;
    private String sellerId;

    private String imageUrl;

    private List<String> categoryIds = new ArrayList<>();

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
