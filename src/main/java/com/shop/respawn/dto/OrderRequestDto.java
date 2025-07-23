package com.shop.respawn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class OrderRequestDto {

    @NotNull(message = "배송 주소 ID는 필수입니다")
    private Long addressId;

    // 선택 주문 시 사용할 장바구니 아이템 ID 목록
    private List<Long> cartItemIds;
}
