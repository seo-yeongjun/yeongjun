package com.yeongjun.yeongjun.hyerin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NyanghwagwaSaleRequest {

    @NotNull(message = "세트 ID는 필수입니다.")
    private Long setId;

    @NotNull(message = "판매 수량은 필수입니다.")
    @Min(value = 1, message = "판매 수량은 1 이상이어야 합니다.")
    private Integer count;

    private String notes;
}
