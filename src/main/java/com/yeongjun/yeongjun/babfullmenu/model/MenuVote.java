package com.yeongjun.yeongjun.babfullmenu.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MenuVote {
    private Long id;
    private LocalDate menuDate;
    private String menuName;
    private String voteType; // "LIKE" or "DISLIKE"
    private String sessionId; // session ID or cookie ID
    private LocalDateTime createdAt;
}
