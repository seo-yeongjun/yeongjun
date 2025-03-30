package com.yeongjun.yeongjun.coffeeGame.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoffeeGameRecord {
    private Long id;
    private LocalDate date;
    private String game_name;
    private String loser_name;
    private String purchase_items;
    private int cost;
}