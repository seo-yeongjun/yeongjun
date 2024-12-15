package com.yeongjun.yeongjun.home.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Category {
    private int category_id;
    private String category_name;
    private int parent_id;
    private String description;
    private String path;
}
