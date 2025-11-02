package com.yeongjun.yeongjun.hyerin.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NyanghwagwaItem {
    private Long item_id;
    private String item_name;
    private Integer quantity;
    private String unit;
    private String description;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
