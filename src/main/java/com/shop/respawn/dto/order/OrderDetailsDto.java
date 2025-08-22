package com.shop.respawn.dto.order;

import com.mongodb.lang.Nullable;
import com.shop.respawn.domain.Address;
import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Order;
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

    // 아이템 정보
    private int itemCount;           // 총 상품 개수
    private Long itemTotalAmount;    // 상품 합계 금액
    private Long totalDeliveryFee;   // 배송비 합계
    private Long totalAmount;        // 최종 결제 금액

    // 배송지 정보
    @Nullable private Long addressId;
    @Nullable private String addressName;
    @Nullable private String recipient;
    @Nullable private String zoneCode;
    @Nullable private String baseAddress;
    @Nullable private String detailAddress;
    @Nullable private String addressPhone;

    public OrderDetailsDto(Order order, Buyer buyer, List<OrderItemDetailDto> orderItems, int itemCount,
                           Long itemTotalAmount, Long totalDeliveryFee, Long totalAmount, Address address) {
        this.orderId = order.getId();
        // 구매자
        this.name = buyer.getName();
        this.phoneNumber = buyer.getPhoneNumber();
        this.email = buyer.getEmail();
        // 주문 아이템 목록
        this.orderItems = orderItems != null ? List.copyOf(orderItems) : List.of();
        // 아이템 정보
        this.itemCount = itemCount;
        this.itemTotalAmount = itemTotalAmount;
        this.totalDeliveryFee = totalDeliveryFee;
        this.totalAmount = totalAmount;
        // 배송지 정보 - null 안전 처리
        if (address != null) {
            this.addressId = address.getId();
            this.addressName = address.getAddressName();
            this.recipient = address.getRecipient();
            this.zoneCode = address.getZoneCode();
            this.baseAddress = address.getBaseAddress();
            this.detailAddress = address.getDetailAddress();
            this.addressPhone = address.getPhone();
        } else {
            this.addressId = null;
            this.addressName = null;
            this.recipient = null;
            this.zoneCode = null;
            this.baseAddress = null;
            this.detailAddress = null;
            this.addressPhone = null;
        }
    }
}
