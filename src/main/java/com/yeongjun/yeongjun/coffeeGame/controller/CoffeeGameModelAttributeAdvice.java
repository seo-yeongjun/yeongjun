package com.yeongjun.yeongjun.coffeeGame.controller;

import com.yeongjun.yeongjun.global.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CoffeeGameModelAttributeAdvice {

    private final CategoryService categoryService;

    public CoffeeGameModelAttributeAdvice(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ModelAttribute
    public void transactionsAttributes(HttpServletRequest request, Model model) {
        if (request.getRequestURI().startsWith("/coffeeGame")) {
            model.addAttribute("coffeeGameCategory", categoryService.getCoffeeGameCategory());
            model.addAttribute("title", "커피게임 ☕️🎲");
        }
    }

    @ModelAttribute
    public void gogoClubtransactionsAttributes(HttpServletRequest request, Model model) {
        if (request.getRequestURI().startsWith("/coffeeGame/gogoClubStat")) {
            model.addAttribute("gogoClubStatCategory", categoryService.getGogoClubStatCategory());
            model.addAttribute("title", "고고클럽 통계");
        }
    }

}
