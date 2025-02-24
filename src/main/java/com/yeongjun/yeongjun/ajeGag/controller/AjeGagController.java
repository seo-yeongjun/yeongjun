package com.yeongjun.yeongjun.ajeGag.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ajeGag")
@Slf4j
public class AjeGagController {

    @GetMapping({"", "/"})
    public String getNewsBySearch(Model model) {
        return "ajeGag/home";
    }
}