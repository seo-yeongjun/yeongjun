package com.yeongjun.yeongjun.hyerin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NyanghwagwaItemViewDto {
    private final Long itemId;
    private final String itemName;
    private final Integer quantity;
    private final String unit;
    private final String description;
}
