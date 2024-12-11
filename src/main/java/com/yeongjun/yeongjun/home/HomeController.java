package com.yeongjun.yeongjun.home;

import com.yeongjun.yeongjun.Security.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

    @GetMapping("/home")
    public String homePage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("title", "홈 페이지");
        model.addAttribute("message", "Welcome to the Home Page!");
        if (user != null) {
            model.addAttribute("user", user.getNickname());
        }
        return "home"; // home.html 반환
    }
}
