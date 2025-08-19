package com.shop.respawn.dto.order;

import com.shop.respawn.domain.*;
import com.shop.respawn.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Order 엔티티 -> OrderDetailsDto 변환 전용 클래스
 * (서비스의 책임을 분리하여 가독성과 재사용성을 높임)
 */
@Component
@RequiredArgsConstructor
public class OrderDetailsAssembler {

    private final ItemRepository itemRepository;

    public OrderDetailsDto toDto(Order order, Address buyerAddress) {
        List<OrderItemDetailDto> orderItemDetails = order.getOrderItems().stream()
                .map(orderItem -> {
                    Item item = itemRepository.findById(orderItem.getItemId())
                            .orElseThrow(() ->
                                    new RuntimeException("상품을 찾을 수 없습니다: " + orderItem.getItemId())
                            );
                    return OrderItemDetailDto.from(orderItem, item);
                }).toList();

        long totalItemAmount = orderItemDetails.stream()
                .mapToLong(OrderItemDetailDto::getTotalPrice)
                .sum();

        long totalDeliveryFee = orderItemDetails.stream()
                .map(OrderItemDetailDto::getDeliveryFee)
                .distinct()
                .mapToLong(Long::longValue)
                .sum();

        long totalAmount = totalItemAmount + totalDeliveryFee;

        return OrderDetailsDto.builder()
                .orderId(order.getId())
                // 구매자 정보
                .name(order.getBuyer().getName())
                .phoneNumber(order.getBuyer().getPhoneNumber())
                .email(order.getBuyer().getEmail())
                // 주문 아이템 정보
                .orderItems(orderItemDetails)
                .itemCount(orderItemDetails.size())
                .itemTotalAmount(totalItemAmount)
                .totalDeliveryFee(totalDeliveryFee)
                .totalAmount(totalAmount)
                // 배송지 정보
                .addressId(buyerAddress.getId())
                .addressName(buyerAddress.getAddressName())
                .recipient(buyerAddress.getRecipient())
                .zoneCode(buyerAddress.getZoneCode())
                .baseAddress(buyerAddress.getBaseAddress())
                .detailAddress(buyerAddress.getDetailAddress())
                .addressPhone(buyerAddress.getPhone())
                .build();
    }
}
