package com.shop.respawn.dto;

import com.shop.respawn.domain.Order;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderHistoryDto {

    private Long orderId;
    private LocalDateTime orderDate;
    private int totalAmount;
    private List<OrderHistoryItemDto> items;

    // 생성자
    public OrderHistoryDto(Order order, List<OrderHistoryItemDto> items) {
        this.orderId = order.getId();
        this.orderDate = order.getOrderDate();
        this.totalAmount = order.getTotalAmount();
        this.items = items;
    }
}
