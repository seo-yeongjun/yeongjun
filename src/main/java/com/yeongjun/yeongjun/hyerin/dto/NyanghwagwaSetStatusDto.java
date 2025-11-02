package com.yeongjun.yeongjun.hyerin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NyanghwagwaSetStatusDto {
    private final Long setId;
    private final String setName;
    private final Integer stockQuantity;
    private final Integer totalItemCount;
}
