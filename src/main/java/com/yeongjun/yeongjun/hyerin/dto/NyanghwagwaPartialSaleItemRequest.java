package com.yeongjun.yeongjun.hyerin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NyanghwagwaPartialSaleItemRequest {

    @NotNull(message = "화과자를 선택해 주세요.")
    private Long itemId;

    @NotNull(message = "판매 수량을 입력해 주세요.")
    @Min(value = 1, message = "판매 수량은 1 이상이어야 합니다.")
    private Integer quantity;
}
