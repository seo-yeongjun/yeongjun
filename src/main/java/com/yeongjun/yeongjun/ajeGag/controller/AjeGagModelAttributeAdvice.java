package com.yeongjun.yeongjun.ajeGag.controller;

import com.yeongjun.yeongjun.global.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AjeGagModelAttributeAdvice {

    @ModelAttribute
    public void transactionsAttributes(HttpServletRequest request, Model model) {
        if (request.getRequestURI().startsWith("/ajeGag")) {
            model.addAttribute("title", "아재개그 박물관");
        }
    }

}
