package com.yeongjun.yeongjun.babfullmenu.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MenuLikeDislike {
    private Long id;
    private LocalDate menuDate;
    private String menuName;
    private Integer likeCount;
    private Integer dislikeCount;
}
