package com.shop.respawn.dto;

import lombok.Data;

import java.util.List;

@Data
public class ItemDto {

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

    private List<String> categoryIds;

    public ItemDto(String id, String name, String description, String deliveryType, String deliveryFee, String company, int price, int stockQuantity, String sellerId, String imageUrl, List<String> categoryIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.deliveryType = deliveryType;
        this.deliveryFee = deliveryFee;
        this.company = company;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.sellerId = sellerId;
        this.imageUrl = imageUrl;
        this.categoryIds = categoryIds;
    }
}
