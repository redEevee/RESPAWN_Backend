package com.shop.respawn.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuantityChangeRequest {

    @NotNull(message = "변경할 수량을 입력해주세요.")
    @Positive(message = "변경할 수량은 0보다 커야 합니다.")
    private Integer amount;
}
