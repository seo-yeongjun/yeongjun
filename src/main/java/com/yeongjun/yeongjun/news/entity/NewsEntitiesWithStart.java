package com.yeongjun.yeongjun.news.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsEntitiesWithStart {
    private List<NewsEntity> newsEntities;
    private int display;
    private int start;

    public NewsEntitiesWithStart(int display, int start) {
        this.newsEntities = new ArrayList<>();
        this.display = display;
        this.start = start;
    }
}
