package com.yeongjun.yeongjun.hyerin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NyanghwagwaSetComponentRequest {

    @NotNull(message = "아이템 ID는 필수입니다.")
    private Long itemId;

    @NotNull(message = "필요 수량을 입력해 주세요.")
    @Min(value = 1, message = "필요 수량은 1 이상이어야 합니다.")
    private Integer requiredQuantity;
}
