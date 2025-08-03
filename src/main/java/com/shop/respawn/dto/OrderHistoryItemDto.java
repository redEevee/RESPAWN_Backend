package com.shop.respawn.dto;

import com.shop.respawn.domain.DeliveryStatus;
import com.shop.respawn.domain.Item;
import com.shop.respawn.domain.OrderItem;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class OrderHistoryItemDto {

    private Long orderItemId;
    private String itemId;
    private String itemName;
    private int orderPrice;                 // 주문 시 가격
    private int count;                      // 수량
    private String imageUrl;                // 이미지

    private LocalDateTime requestedAt;      // 환불 요청일
    private String refundReason;            // 환불 사유
    private String refundDetail;            // 상세 내역
    private String refundStatus;            // 문자열로 환불 상태 전달

    private DeliveryStatus deliveryStatus;  //배송 상태

    // 생성자
    private OrderHistoryItemDto() {
    }

    public static OrderHistoryItemDto from(OrderItem orderItem, Item item) {
        OrderHistoryItemDto dto = new OrderHistoryItemDto();
        dto.setOrderItemId(orderItem.getId());
        dto.setItemId(orderItem.getItemId());
        dto.setItemName(item.getName());
        dto.setOrderPrice(orderItem.getOrderPrice());
        dto.setCount(orderItem.getCount());
        dto.setImageUrl(item.getImageUrl());
        dto.setRefundStatus(orderItem.getRefundStatus().name());
        dto.setDeliveryStatus(orderItem.getDelivery().getStatus());

        if(orderItem.getRefundRequest() != null) {
            dto.setRefundReason(orderItem.getRefundRequest().getRefundReason());
            dto.setRefundDetail(orderItem.getRefundRequest().getRefundDetail());
            dto.setRequestedAt(orderItem.getRefundRequest().getRequestedAt());
        }

        return dto;
    }
}
