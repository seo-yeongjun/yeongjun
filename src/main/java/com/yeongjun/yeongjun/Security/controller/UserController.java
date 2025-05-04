package com.yeongjun.yeongjun.Security.controller;

import com.yeongjun.yeongjun.Security.dto.RegisterRequest;
import com.yeongjun.yeongjun.Security.model.Role;
import com.yeongjun.yeongjun.Security.model.User;
import com.yeongjun.yeongjun.Security.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    @Value("${spring.profiles.active}")
    private String activeProfile;

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
                            @RequestParam(value = "rememberMe", required = false) boolean rememberMe,
                            HttpServletResponse response,
                            Model model) {
        log.info("로그인 시도: username={}, rememberMe={}", username, rememberMe);
        try {
            String token = userService.loginUser(username, password, rememberMe);
            log.info("로그인 성공: username={}", username);
            System.out.println("로그인 성공: username=" + username);

            // JWT를 쿠키에 저장
            Cookie cookie = new Cookie("authToken", token);
            cookie.setHttpOnly(true); // JavaScript에서 접근 불가 (XSS 방지)
            cookie.setSecure("prod".equals(activeProfile));  // 운영 환경에서만 HTTPS 사용
            cookie.setPath("/");     // 모든 경로에 대해 쿠키 전송
            cookie.setMaxAge(rememberMe ? 30 * 24 * 60 * 60 : -1); // 30일 또는 세션 종료시 만료
            response.addCookie(cookie);
            log.info("authToken 쿠키 생성: value={}, maxAge={}, secure={}", token, cookie.getMaxAge(), cookie.getSecure());

            return "redirect:/"; // 로그인 후 리디렉션
        } catch (IllegalArgumentException e) {
            log.warn("로그인 실패: username={}, 이유={}", username, e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure("prod".equals(activeProfile));  // 운영 환경에서만 HTTPS 사용
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
        return "redirect:/auth/login";
    }

    // 회원가입 화면
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new RegisterRequest());
        return "auth/register"; // register.html
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterRequest registerRequest, Model model) {
        try {
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(registerRequest.getPassword());
            user.setEmail(registerRequest.getEmail());
            user.setNickname(registerRequest.getNickname());
            // Set default values
            user.setRole(Role.USER); // Assuming Role.USER is default
            user.setCreated_at(LocalDateTime.now());
            user.setUpdated_at(LocalDateTime.now());
            userService.registerUser(user);
            return "redirect:/auth/login?success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", registerRequest);
            return "auth/register";
        }
    }

    @GetMapping("privacy")
    public String privacyPage(Model model) {
        return "auth/privacy";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        boolean success = userService.verifyEmail(token);
        model.addAttribute("success", success);
        return "auth/verify-email";
    }

    @GetMapping("/check-email")
    @ResponseBody
    public Map<String, Boolean> checkEmail(@RequestParam("email") String email) {
        boolean exists = userService.isEmailExists(email);
        return java.util.Collections.singletonMap("exists", exists);
    }
}
