package com.yeongjun.yeongjun.news.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsEntity {
    private Long news_id;
    private String search;
    private String company;
    private String title;
    private String link;
    private String description;
    private LocalDateTime pub_date;
    private LocalDateTime created_at;
}
