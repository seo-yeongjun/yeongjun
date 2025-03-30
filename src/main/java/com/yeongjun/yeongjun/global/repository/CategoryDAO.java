package com.yeongjun.yeongjun.global.repository;

import com.yeongjun.yeongjun.home.model.Category;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryDAO extends BaseDAO<Category> {
    @Override
    protected String getNamespace() {
        return "base.category";
    }

    //home
    public List<Category> selectAllBaseCategory() {
        return selectList("selectAllBaseCategory", null);
    }

    //transactions
    public List<Category> selectAllTransactionsCategory() {
        return selectList("selectAllTransactionsCategory", null);
    }

    public List<Category> selectAllToolsCategory() {
        return selectList("selectAllToolsCategory", null);
    }

    public List<Category> selectAllCoffeeGameCategory() {
        return selectList("selectAllCoffeeGameCategory", null);
    }

    public List<Category> selectAllNewsCategory() {
        return selectList("selectAllNewsCategory", null);
    }

    public List<Category> selectAllGogoClubStatCategory() {return selectList("selectAllGogoClubStatCategory", null);}
}

