package com.yeongjun.yeongjun.hyerin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NyanghwagwaSetViewDto {
    private final Long setId;
    private final String setName;
    private final String description;
    private final List<NyanghwagwaSetComponentDto> items;
    private final boolean requireAlert;
}
