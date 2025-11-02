package com.yeongjun.yeongjun.hyerin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NyanghwagwaSetUpsertRequest {

    @NotBlank(message = "세트 이름을 입력해 주세요.")
    private String setName;

    private String description;

    @Valid
    private List<NyanghwagwaSetComponentRequest> components;
}
