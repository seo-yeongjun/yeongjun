package com.yeongjun.yeongjun.coffeeGame.controller;

import com.yeongjun.yeongjun.global.service.CategoryService;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class CoffeeGameModelAttributeAdvice {

    private final CategoryService categoryService;

    public CoffeeGameModelAttributeAdvice(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

//    @ModelAttribute
//    public void transactionsAttributes(HttpServletRequest request, Model model) {
//        if (request.getRequestURI().startsWith("/coffeeGame")) {
//            model.addAttribute("coffeeGameCategory", categoryService.getCoffeeGameCategory());
//            model.addAttribute("title", "커피게임 ☕️🎲");
//        }
//    }

}
