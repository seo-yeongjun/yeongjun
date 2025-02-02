package com.yeongjun.yeongjun.news.controller;

import com.yeongjun.yeongjun.global.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NewsModelAttributeAdvice {

    private final CategoryService categoryService;

    public NewsModelAttributeAdvice(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ModelAttribute
    public void transactionsAttributes(HttpServletRequest request, Model model) {
        if (request.getRequestURI().startsWith("/news")) {
            model.addAttribute("newsCategory", categoryService.getNewsCategory());
            model.addAttribute("title", "타이틀 뉴스 검색");
        }
    }

}
