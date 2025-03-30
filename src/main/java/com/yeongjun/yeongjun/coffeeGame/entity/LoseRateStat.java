package com.yeongjun.yeongjun.coffeeGame.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoseRateStat {
    private String loser_name;
    private int lose_count;
    private double lose_rate;
} 