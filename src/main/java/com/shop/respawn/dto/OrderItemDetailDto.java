package com.shop.respawn.dto;

import com.shop.respawn.domain.Item;
import com.shop.respawn.domain.OrderItem;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class OrderItemDetailDto {
    private Long orderItemId;
    private String itemId;
    private String itemName;
    private String itemDescription;
    private Integer itemPrice;
    private Integer orderPrice;
    private Integer count;
    private Integer totalPrice;
    private String imageUrl;
    private Integer stockQuantity;

    // 생성자
    private OrderItemDetailDto(OrderItem orderItem, Item item) {
        this.orderItemId = orderItem.getId();
        this.itemId = orderItem.getItemId();
        this.itemName = item.getName();
        this.itemDescription = item.getDescription();
        this.itemPrice = item.getPrice();
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

    // Map으로 변환하는 메서드
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("orderItemId", this.orderItemId);
        map.put("itemId", this.itemId);
        map.put("itemName", this.itemName);
        map.put("itemDescription", this.itemDescription);
        map.put("itemPrice", this.itemPrice);
        map.put("orderPrice", this.orderPrice);
        map.put("count", this.count);
        map.put("totalPrice", this.totalPrice);
        map.put("imageUrl", this.imageUrl);
        map.put("stockQuantity", this.stockQuantity);
        return map;
    }
}
