package com.yeongjun.yeongjun.home.controller;

import com.yeongjun.yeongjun.global.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

    private final CategoryService categoryService;

    public HomeController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("title", "홈 페이지");
        model.addAttribute("baseCategory", categoryService.getBaseCategoryList());
        return "home"; // home.html 반환
    }
}
