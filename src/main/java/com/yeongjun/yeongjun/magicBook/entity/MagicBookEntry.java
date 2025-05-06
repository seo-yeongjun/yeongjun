package com.yeongjun.yeongjun.magicBook.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MagicBookEntry {
    private Long id;
    private String category;
    private String answer;
}
