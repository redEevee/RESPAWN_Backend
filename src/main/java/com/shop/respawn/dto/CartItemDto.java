package com.shop.respawn.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartItemDto {

    private Long buyerId;

    private String itemId;

    private int price;

    private int count;

}
