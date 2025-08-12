package com.shop.respawn.dto;

import com.shop.respawn.domain.Item;
import com.shop.respawn.domain.OrderItem;
import lombok.Data;

@Data
public class OrderItemDetailDto {
    private Long orderItemId;
    private String itemId;
    private String itemName;
    private String itemDescription;
    private Long itemPrice;
    private Long deliveryFee;
    private Long orderPrice;
    private Long count;
    private Long totalPrice;
    private String imageUrl;
    private long stockQuantity;

    // 생성자
    private OrderItemDetailDto(OrderItem orderItem, Item item) {
        this.orderItemId = orderItem.getId();
        this.itemId = orderItem.getItemId();
        this.itemName = item.getName();
        this.itemDescription = item.getDescription();
        this.itemPrice = item.getPrice();
        this.deliveryFee = item.getDeliveryFee();
        this.orderPrice = orderItem.getOrderPrice();
        this.count = orderItem.getCount();
        this.totalPrice = orderItem.getOrderPrice() * orderItem.getCount();
        this.imageUrl = item.getImageUrl();
        this.stockQuantity = item.getStockQuantity();
    }

    // 정적 팩토리 메서드
    public static OrderItemDetailDto from(OrderItem orderItem, Item item) {
        return new OrderItemDetailDto(orderItem, item);
    }

}
