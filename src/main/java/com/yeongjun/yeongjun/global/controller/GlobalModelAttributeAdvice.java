package com.yeongjun.yeongjun.global.controller;

import com.yeongjun.yeongjun.Security.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributeAdvice {
    @ModelAttribute("user")
    public String addUserToModel(@AuthenticationPrincipal User user) {
        return user != null ? user.getUsername() : null;
    }
}