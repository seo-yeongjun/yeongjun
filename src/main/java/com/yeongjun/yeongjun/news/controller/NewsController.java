package com.yeongjun.yeongjun.news.controller;

import com.yeongjun.yeongjun.news.entity.NewsEntitiesWithStart;
import com.yeongjun.yeongjun.news.repository.NewsCompanyDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/news")
@Slf4j
public class NewsController {

    private final NewsService newsService;
    private LocalDateTime lastSokbo;
    private NewsEntitiesWithStart sokboNewsEntities;

    // "속보" 캐싱 로직에 대한 동기화를 위한 별도 락 객체
    private final Object sokboLock = new Object();

    public NewsController(NewsService newsService, NewsCompanyDAO newsCompanyDAO) {
        this.newsService = newsService;
        this.sokboNewsEntities = new NewsEntitiesWithStart(100, 1);
        this.lastSokbo = null;
    }

    @GetMapping({"", "/", "home"})
    public String getNewsBySearch(@RequestParam(required = false, defaultValue = "속보") String search,
                                  @RequestParam(required = false, defaultValue = "1") int start,
                                  @RequestParam(required = false, defaultValue = "100") int display,
                                  Model model) {
        if ("속보".equals(search)) {
            synchronized (sokboLock) {
                // 저장된 속보가 없거나 15분 이상 경과했을 경우 업데이트
                if (lastSokbo == null || lastSokbo.isBefore(LocalDateTime.now().minusMinutes(15))) {
                    lastSokbo = LocalDateTime.now();
                    sokboNewsEntities = newsService.getNewsBySearch(search, sokboNewsEntities);
                    log.info("속보 갱신: {}", sokboNewsEntities);
                }
                model.addAttribute("newsEntities", sokboNewsEntities);
            }
        } else {
            model.addAttribute("newsEntities", newsService.getNewsBySearch(search, new NewsEntitiesWithStart(display, start)));
        }
        model.addAttribute("newsCompanyList", newsService.getNewsCompanyList());
        return "news/home";
    }
}