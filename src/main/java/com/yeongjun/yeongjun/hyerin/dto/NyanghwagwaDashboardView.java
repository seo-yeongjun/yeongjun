package com.yeongjun.yeongjun.hyerin.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NyanghwagwaDashboardView {
    private final List<NyanghwagwaSetStatusDto> setStatuses;
    private final List<NyanghwagwaSetViewDto> produceCandidates;
    private final List<NyanghwagwaSetViewDto> saleCandidates;
    private final List<NyanghwagwaItemViewDto> itemList;
    private final List<NyanghwagwaSetViewDto> setList;
}
