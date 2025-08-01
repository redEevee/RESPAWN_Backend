package com.shop.respawn.dto;

import com.shop.respawn.domain.Item;
import com.shop.respawn.domain.Order;
import com.shop.respawn.domain.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class SellerOrderDto {
    private Long orderId;               // 주문번호
    private String buyerName;           // 구매자 이름
    private String itemName;            // 상품명
    private int count;                  // 수량
    private int totalPrice;             // 결제 금액
    private String orderStatus;         // 주문 상태
    private LocalDateTime orderDate;    // 주문 일시

    public SellerOrderDto(Order order, OrderItem orderItem, Item item) {
        this.orderId = order.getId();
        this.buyerName = order.getBuyer().getName();
        this.itemName = item.getName();
        this.count = orderItem.getCount();
        this.totalPrice = orderItem.getOrderPrice() * orderItem.getCount();
        this.orderStatus = order.getStatus().name();
        this.orderDate = order.getOrderDate();
    }
}
