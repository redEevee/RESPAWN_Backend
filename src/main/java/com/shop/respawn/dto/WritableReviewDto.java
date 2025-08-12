package com.shop.respawn.dto;

import lombok.Data;

@Data
public class WritableReviewDto {
    private Long orderId;
    private String orderItemId;
    private String itemName;
    private String itemImage;
    private boolean reviewExists;

    public WritableReviewDto(Long orderId, String orderItemId, String itemName, String itemImage, boolean reviewExists) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.itemName = itemName;
        this.itemImage = itemImage;
        this.reviewExists = reviewExists;
    }
}