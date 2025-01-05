package com.yeongjun.yeongjun.babfullmenu.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuDTO {
    private String start_dt;
    private String end_dt;
    private String is_menu;

    private List<String> morning_day1 = new ArrayList<>();
    private List<String> morning_day2 = new ArrayList<>();
    private List<String> morning_day3 = new ArrayList<>();
    private List<String> morning_day4 = new ArrayList<>();
    private List<String> morning_day5 = new ArrayList<>();

    private List<String> lunch_day1 = new ArrayList<>();
    private List<String> lunch_day2 = new ArrayList<>();
    private List<String> lunch_day3 = new ArrayList<>();
    private List<String> lunch_day4 = new ArrayList<>();
    private List<String> lunch_day5 = new ArrayList<>();

    private List<String> lunch_names = new ArrayList<>();
}
