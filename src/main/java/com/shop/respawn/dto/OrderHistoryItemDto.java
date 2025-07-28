package com.shop.respawn.dto;

import com.shop.respawn.domain.Item;
import com.shop.respawn.domain.OrderItem;
import lombok.Getter;

@Getter
public class OrderHistoryItemDto {

    private String itemId;
    private String itemName;
    private int price;       // 주문 시 가격
    private int count;       // 수량
    private String imageUrl;     // 이미지

    // 생성자
    private OrderHistoryItemDto(OrderItem orderItem, Item item) {
        this.itemId = item.getId();
        this.itemName = item.getName();
        this.price = orderItem.getOrderPrice();
        this.count = orderItem.getCount();
        this.imageUrl = item.getImageUrl();
    }

    // 정적 팩토리 메서드
    public static OrderHistoryItemDto from(OrderItem orderItem, Item item) {
        return new OrderHistoryItemDto(orderItem, item);
    }
}
