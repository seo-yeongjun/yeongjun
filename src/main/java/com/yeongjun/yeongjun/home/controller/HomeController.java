package com.yeongjun.yeongjun.home.controller;

import com.yeongjun.yeongjun.Security.model.Role;
import com.yeongjun.yeongjun.Security.model.User;
import com.yeongjun.yeongjun.global.service.CategoryService;
import com.yeongjun.yeongjun.home.model.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Slf4j
public class HomeController {

    private final CategoryService categoryService;

    public HomeController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String homePage(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("title", "홈 페이지");
        List<Category> baseCategory = categoryService.getBaseCategoryList();
        if(user != null){
            if(user.getRole() == Role.ADMIN || user.getRole() == Role.HYERIN) {
                Category hyerinCategory = new Category();
                hyerinCategory.setCategory_name("혜린");
                hyerinCategory.setPath("/hyerin");
                hyerinCategory.setDescription("혜린이의 포트폴리오 페이지입니다.");
                baseCategory.add(hyerinCategory);
            }
        }
        model.addAttribute("baseCategory", baseCategory);
        return "home"; // home.html 반환
    }
}
