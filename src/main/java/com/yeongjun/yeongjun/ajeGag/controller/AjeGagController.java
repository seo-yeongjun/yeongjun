package com.yeongjun.yeongjun.ajeGag.controller;

import com.yeongjun.yeongjun.news.controller.NewsService;
import com.yeongjun.yeongjun.news.repository.NewsCompanyDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ajeGag")
@Slf4j
public class AjeGagController {

    private final NewsService newsService;

    public AjeGagController(NewsService newsService, NewsCompanyDAO newsCompanyDAO) {
        this.newsService = newsService;
    }

    @GetMapping({"", "/"})
    public String getNewsBySearch(Model model) {
        return "ajeGag/home";
    }
}