package com.shop.respawn.dto;

import lombok.Data;

import java.util.List;

@Data
public class ItemDto {

    private String name;

    private String description;

    private String wireless;

    private String deliveryType;

    private String deliveryFee;

    private int price;

    private int stockQuantity;

    private String sellerId;

    private String imageUrl;

    private List<String> categoryIds;

}
