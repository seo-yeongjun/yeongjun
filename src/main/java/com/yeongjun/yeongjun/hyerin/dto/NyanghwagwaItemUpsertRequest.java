package com.yeongjun.yeongjun.hyerin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NyanghwagwaItemUpsertRequest {

    @NotBlank(message = "아이템 이름을 입력해 주세요.")
    private String itemName;

    @NotNull(message = "재고 수량을 입력해 주세요.")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private Integer quantity;

    private String unit;
    private String description;
}
