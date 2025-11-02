package com.yeongjun.yeongjun.hyerin.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NyanghwagwaInventoryLog {
    private Long log_id;
    private NyanghwagwaInventoryLogType log_type;
    private Long set_id;
    private Long item_id;
    private Integer quantity_change;
    private Integer remaining_stock;
    private LocalDateTime performed_at;
    private String actor_username;
    private String notes;
}
