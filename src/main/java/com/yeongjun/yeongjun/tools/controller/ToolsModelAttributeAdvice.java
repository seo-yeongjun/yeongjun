package com.yeongjun.yeongjun.tools.controller;

import com.yeongjun.yeongjun.global.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ToolsModelAttributeAdvice {

    private final CategoryService categoryService;

    public ToolsModelAttributeAdvice(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ModelAttribute
    public void transactionsAttributes(HttpServletRequest request, Model model) {
        if (request.getRequestURI().startsWith("/tools")) {
            model.addAttribute("toolsCategory", categoryService.getToolsCategory());
            model.addAttribute("title", "도구상자");
        }
    }

}