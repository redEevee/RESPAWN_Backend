package com.shop.respawn.dto;

import com.shop.respawn.domain.*;
import lombok.*;

import java.time.LocalDateTime;


@Data
public class SellerOrderDetailDto {

    // 주문 정보
    private Long orderItemId;
    private Long orderId;
    private String itemName;
    private String imageUrl;
    private int orderPrice;
    private int count;
    private int totalPrice;
    private LocalDateTime orderDate;
    private String orderStatus;

    // 구매자 정보
    private String buyerName;
    private String buyerPhone;
    private String buyerEmail;

    // 배송지 정보
    private String recipient;
    private String zoneCode;
    private String baseAddress;
    private String detailAddress;
    private String phone;
    private String subPhone;

    private String deliveryStatus;

    public SellerOrderDetailDto(Order order, OrderItem orderItem, Item item, Buyer buyer, Delivery delivery) {
        this.orderItemId = orderItem.getId();
        this.orderId = order.getId();
        this.itemName = item.getName();
        this.imageUrl = item.getImageUrl();
        this.orderPrice = orderItem.getOrderPrice();
        this.count = orderItem.getCount();
        this.totalPrice = orderItem.getOrderPrice() * orderItem.getCount();
        this.orderDate = order.getOrderDate();
        this.orderStatus = order.getStatus().name();

        this.buyerName = buyer.getName();
        this.buyerPhone = buyer.getPhoneNumber();
        this.buyerEmail = buyer.getEmail();

        if (delivery != null && delivery.getAddress() != null) {
            Address address = delivery.getAddress();
            this.recipient = address.getRecipient();
            this.zoneCode = address.getZoneCode();
            this.baseAddress = address.getBaseAddress();
            this.detailAddress = address.getDetailAddress();
            this.phone = address.getPhone();
            this.subPhone = address.getSubPhone();
        }

        this.deliveryStatus = delivery != null && delivery.getStatus() != null ? delivery.getStatus().name() : null;
    }
}