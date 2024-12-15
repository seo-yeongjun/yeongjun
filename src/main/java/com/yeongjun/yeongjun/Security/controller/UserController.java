package com.yeongjun.yeongjun.Security.controller;

import com.yeongjun.yeongjun.Security.model.User;
import com.yeongjun.yeongjun.Security.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 로그인 화면
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "로그인");
        return "auth/login"; // login.html 반환
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            HttpServletResponse response,
                            Model model) {
        try {
            String token = userService.loginUser(username, password);

            // JWT를 쿠키에 저장
            Cookie cookie = new Cookie("authToken", token);
            cookie.setHttpOnly(true); // JavaScript에서 접근 불가 (XSS 방지)
            cookie.setSecure(true);  // HTTPS에서만 전송
            cookie.setPath("/");     // 모든 경로에 대해 쿠키 전송
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7일간 유효
            response.addCookie(cookie);

            return "redirect:/home"; // 로그인 후 리디렉션
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
        return "redirect:/auth/login";
    }

    // 회원가입 화면
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register"; // register.html
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user);
            return "redirect:/auth/login?success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}
