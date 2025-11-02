package com.yeongjun.yeongjun.hyerin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NyanghwagwaProduceRequest {

    private Long setId;

    private Long itemId;

    @NotNull(message = "제작 수량은 필수입니다.")
    @Min(value = 1, message = "제작 수량은 1 이상이어야 합니다.")
    private Integer count;

    private String notes;
}
