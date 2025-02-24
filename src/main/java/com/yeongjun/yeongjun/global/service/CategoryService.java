package com.yeongjun.yeongjun.global.service;

import com.yeongjun.yeongjun.global.repository.CategoryDAO;
import com.yeongjun.yeongjun.home.model.Category;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryDAO categoryDAO;

    // 캐시 역할을 할 멤버 변수들
    @Getter
    private List<Category> baseCategoryList;
    @Getter
    private List<Category> transactionsCategoryList;
    @Getter
    private List<Category> toolsCategory;
    @Getter
    private List<Category> coffeeGameCategory;
    @Getter
    private List<Category> newsCategory;

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    /**
     * 애플리케이션 시작 시 또는 CategoryService 생성 직후에 카테고리 정보를 초기화
     */
    @PostConstruct
    public void initCategoryCache() {
        updateCategoryCache();
    }

    /**
     * 매 시간 정각(0분)에 카테고리 캐시를 업데이트합니다.
     * cron 표현식 "0 0 * * * *"은 매 시간 정각을 의미합니다.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updateCategoryCache() {
        baseCategoryList = categoryDAO.selectAllBaseCategory();
        transactionsCategoryList = categoryDAO.selectAllTransactionsCategory();
        toolsCategory = categoryDAO.selectAllToolsCategory();
        coffeeGameCategory = categoryDAO.selectAllCoffeeGameCategory();
        newsCategory = categoryDAO.selectAllNewsCategory();
    }
}
