package com.yeongjun.yeongjun.global.service;

import com.yeongjun.yeongjun.global.repository.CategoryDAO;
import com.yeongjun.yeongjun.home.model.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public List<Category> getBaseCategoryList() {
        return categoryDAO.selectAllBaseCategory();
    }
    public List<Category> getTransactionsCategoryList() {
        return categoryDAO.selectAllTransactionsCategory();
    }
}
