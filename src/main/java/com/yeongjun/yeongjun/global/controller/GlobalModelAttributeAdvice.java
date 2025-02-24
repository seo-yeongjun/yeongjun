package com.yeongjun.yeongjun.global.controller;

import com.yeongjun.yeongjun.Security.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collection;
import java.util.Collections;

@ControllerAdvice
public class GlobalModelAttributeAdvice {
    @ModelAttribute("user")
    public String addUserToModel(@AuthenticationPrincipal User user) {
        return user != null ? user.getUsername() : null;
    }
    @ModelAttribute("isAdmin")
    public boolean isAdmin(@AuthenticationPrincipal User user) {
        return user != null && user.getAuthorities().stream()
                .anyMatch(auth -> "ADMIN".equals(auth.getAuthority()));
    }
}