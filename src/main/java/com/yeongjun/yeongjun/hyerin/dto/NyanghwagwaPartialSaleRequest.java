package com.yeongjun.yeongjun.hyerin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NyanghwagwaPartialSaleRequest {

    /**
     * 낱개 판매를 수행한 세트 ID (선택). 세트 맥락이 없으면 null.
     */
    private Long setId;

    @Valid
    @NotEmpty(message = "최소 1개의 화과자를 선택해 주세요.")
    private List<NyanghwagwaPartialSaleItemRequest> items;

    private String notes;
}
