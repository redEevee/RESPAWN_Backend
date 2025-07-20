package com.shop.respawn.dto;

import lombok.Data;

import java.util.List;

@Data
public class ItemDto {

    private String name;

    private String description;

    private String wireless;

    private int price;

    private int stockQuantity;

    private String sellerId;

    private List<String> categoryIds;

}
