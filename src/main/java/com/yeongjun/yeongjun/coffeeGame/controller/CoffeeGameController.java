package com.yeongjun.yeongjun.coffeeGame.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/coffeeGame")
@Slf4j
public class CoffeeGameController {

    @GetMapping({"", "/"})
    public String coffeeGameHome(Model model) {
        return "coffeeGame/home";
    }

    @GetMapping("/ghostLeg")
    public String ghostLeg(Model model) {
        return "coffeeGame/ghostLeg";
    }

    @GetMapping("/carrot")
    public String carrot(Model model) {
        return "coffeeGame/carrot";
    }

    @GetMapping("/wheel")
    public String wheel(Model model) {
        return "coffeeGame/wheel";
    }
}
