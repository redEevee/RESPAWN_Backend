package com.shop.respawn.dto;

import com.shop.respawn.domain.Item;
import com.shop.respawn.domain.Review;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
public class ReviewWithItemDto {

    private String reviewId;
    private String buyerId;
    private String orderItemId;
    private int rating;
    private String content;
    private LocalDateTime createdDate;

    // 아이템 정보 (필요한 항목만)
    private String itemId;
    private String itemName;
    private String imageUrl;
    private int price;

    public ReviewWithItemDto(Review review, Item item) {
        this.reviewId = review.getId();
        this.buyerId = review.getBuyerId();
        this.orderItemId = review.getOrderItemId();
        this.rating = review.getRating();
        this.content = review.getContent();
        this.createdDate = review.getCreatedDate();

        if(item != null){
            this.itemId = item.getId();
            this.itemName = item.getName();
            this.imageUrl = item.getImageUrl();
            this.price = item.getPrice();
        }
    }
}
