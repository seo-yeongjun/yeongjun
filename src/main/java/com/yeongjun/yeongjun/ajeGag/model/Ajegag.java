package com.yeongjun.yeongjun.ajeGag.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class Ajegag {
    private Integer ajegag_text_id;
    private String title;
    private String detail;
    private LocalDateTime created_at;
}
