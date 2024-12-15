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
        return selectList("selectAllBaseCategory",null);
    }

    //transactions
    public List<Category> selectAllTransactionsCategory() {
        return selectList("selectAllTransactionsCategory",null);
    }
}

