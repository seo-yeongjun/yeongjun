package com.yeongjun.yeongjun.global.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // 상태 코드 확인
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");

        if (statusCode != null) {
            switch (statusCode) {
                case 403:
                    model.addAttribute("message", "You don't have permission to access this page.");
                    return "error/403";
                case 404:
                    model.addAttribute("message", "The page you are looking for does not exist.");
                    return "error/404";
                case 500:
                    model.addAttribute("message", "An unexpected error occurred. Please try again later.");
                    return "error/500";
                default:
                    model.addAttribute("message", "An error occurred. Please try again.");
                    return "error/error";
            }
        }

        model.addAttribute("message", "An error occurred. Please try again.");
        return "error/error";
    }
}
