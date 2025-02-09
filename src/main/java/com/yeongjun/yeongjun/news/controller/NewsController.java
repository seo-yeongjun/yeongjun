package com.yeongjun.yeongjun.news.controller;

import com.yeongjun.yeongjun.news.entity.NewsEntitiesWithStart;
import com.yeongjun.yeongjun.news.repository.NewsCompanyDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/news")
@Slf4j
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService, NewsCompanyDAO newsCompanyDAO) {
        this.newsService = newsService;
    }

    @GetMapping({"", "/", "home"})
    public String getNewsBySearch(@RequestParam(required = false, defaultValue = "속보") String search,
                                  @RequestParam(required = false, defaultValue = "1") int start,
                                  @RequestParam(required = false, defaultValue = "100") int display,
                                  Model model) {
        if ("속보".equals(search)) {
                model.addAttribute("newsEntities", newsService.getNewsBySearch(search, new NewsEntitiesWithStart(display, start)));
        } else {
            model.addAttribute("newsEntities", newsService.getNewsBySearch(search, new NewsEntitiesWithStart(display, start)));
        }
        model.addAttribute("newsCompanyList", newsService.getNewsCompanyList());
        return "news/home";
    }
}