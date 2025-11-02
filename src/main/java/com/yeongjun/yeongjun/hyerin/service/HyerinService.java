package com.yeongjun.yeongjun.hyerin.service;

import com.yeongjun.yeongjun.hyerin.entity.HyerinPortfolioPage;
import com.yeongjun.yeongjun.hyerin.repository.HyerinRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HyerinService {

    private final HyerinRepository hyerinRepository;

    public HyerinService(HyerinRepository hyerinRepository) {
        this.hyerinRepository = hyerinRepository;
    }

    public String getHomeView() {
        return "hyerin/hyerin";
    }

    public String getPortfolioView(String pageKey) {
        return getPortfolioPage(pageKey).getViewName();
    }

    public List<HyerinPortfolioPage> getAllPortfolioPages() {
        return hyerinRepository.findAll();
    }

    public HyerinPortfolioPage getPortfolioPage(String pageKey) {
        return hyerinRepository.findByKey(pageKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 포트폴리오 페이지입니다."));
    }
}
