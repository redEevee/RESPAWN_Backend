package com.shop.respawn.dto;

import com.shop.respawn.domain.Order;
import com.shop.respawn.domain.OrderStatus;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Data
public class OrderHistoryDto {

    private Long orderId;
    private LocalDateTime orderDate;
    private int totalAmount;
    private OrderStatus status;
    private List<OrderHistoryItemDto> items;

    // 생성자
    public OrderHistoryDto(Order order, List<OrderHistoryItemDto> items) {
        this.orderId = order.getId();
        this.orderDate = order.getOrderDate();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.items = items;
    }
}
