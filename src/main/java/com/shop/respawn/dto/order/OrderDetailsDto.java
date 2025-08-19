package com.shop.respawn.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDto {

    private Long orderId;

    // 구매자 정보
    private String name;
    private String phoneNumber;
    private String email;

    // 주문 아이템 목록
    private List<OrderItemDetailDto> orderItems;

    private int itemCount;           // 총 상품 개수
    private Long itemTotalAmount;    // 상품 합계 금액
    private Long totalDeliveryFee;   // 배송비 합계
    private Long totalAmount;        // 최종 결제 금액

    // 배송지 정보
    private Long addressId;
    private String addressName;
    private String recipient;
    private String zoneCode;
    private String baseAddress;
    private String detailAddress;
    private String addressPhone;
}
