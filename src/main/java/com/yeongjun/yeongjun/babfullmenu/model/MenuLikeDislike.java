package com.yeongjun.yeongjun.babfullmenu.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MenuLikeDislike {
    private Long id;
    private String normalizedMenuName; // REGEXP_REPLACE로 정규화된 메뉴명
    private Integer likeCount;
    private Integer dislikeCount;
}

