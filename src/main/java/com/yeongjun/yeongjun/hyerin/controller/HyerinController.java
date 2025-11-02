package com.yeongjun.yeongjun.hyerin.controller;

import com.yeongjun.yeongjun.hyerin.dto.NyanghwagwaDashboardView;
import com.yeongjun.yeongjun.hyerin.entity.HyerinPortfolioPage;
import com.yeongjun.yeongjun.hyerin.service.HyerinService;
import com.yeongjun.yeongjun.hyerin.service.NyanghwagwaInventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
@RequestMapping("/hyerin")
public class HyerinController {

    private final HyerinService hyerinService;
    private final NyanghwagwaInventoryService nyanghwagwaInventoryService;

    public HyerinController(HyerinService hyerinService,
                            NyanghwagwaInventoryService nyanghwagwaInventoryService) {
        this.hyerinService = hyerinService;
        this.nyanghwagwaInventoryService = nyanghwagwaInventoryService;
    }

    @GetMapping({"", "/"})
    public String home() {
        if (!hasHyerinAccess()) {
            return "redirect:/hyerin/login";
        }
        return hyerinService.getHomeView();
    }

    @GetMapping("{pageKey}")
    public String portfolioPage(@PathVariable String pageKey, Model model) {
        if (!hasHyerinAccess()) {
            return "redirect:/hyerin/login";
        }
        HyerinPortfolioPage page = hyerinService.getPortfolioPage(pageKey);

        if ("nyanghwagwa".equalsIgnoreCase(page.getKey())) {
            NyanghwagwaDashboardView dashboardView = nyanghwagwaInventoryService.loadDashboardView();
            model.addAttribute("setStatuses", dashboardView.getSetStatuses());
            model.addAttribute("produceCandidates", dashboardView.getProduceCandidates());
            model.addAttribute("saleCandidates", dashboardView.getSaleCandidates());
            model.addAttribute("itemList", dashboardView.getItemList());
            model.addAttribute("setList", dashboardView.getSetList());
        }

        return page.getViewName();
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
