package com.yeongjun.yeongjun.hyerin.controller;

import com.yeongjun.yeongjun.Security.service.CustomUserDetailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hyerin")
public class HyerinAuthController {

    @Value("${nyanghwagwapass}")
    private String ACCESS_CODE;
    private static final String LOGIN_USER_ID = "wwqeew";

    private final CustomUserDetailService customUserDetailService;

    public HyerinAuthController(CustomUserDetailService customUserDetailService) {
        this.customUserDetailService = customUserDetailService;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (hasHyerinAccess()) {
            return "redirect:/hyerin/nyanghwagwa";
        }
        if (!model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", null);
        }
        return "hyerin/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("accessCode") String accessCode,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        if (!ACCESS_CODE.equals(accessCode)) {
            redirectAttributes.addFlashAttribute("errorMessage", "비밀번호가 올바르지 않습니다.");
            return "redirect:/hyerin/login";
        }

        UserDetails userDetails = customUserDetailService.loadUserByUsername(LOGIN_USER_ID);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return "redirect:/hyerin/nyanghwagwa";
    }

    private boolean hasHyerinAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ADMIN".equalsIgnoreCase(role) || "HYERIN".equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
}
