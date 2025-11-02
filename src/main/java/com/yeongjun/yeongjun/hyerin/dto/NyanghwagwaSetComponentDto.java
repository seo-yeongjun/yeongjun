package com.yeongjun.yeongjun.hyerin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NyanghwagwaSetComponentDto {
    private final Long itemId;
    private final String itemName;
    private final Integer requiredQuantity;
    private final Integer currentQuantity;
    private final String unit;
}
