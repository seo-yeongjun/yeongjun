package com.yeongjun.yeongjun.transactions.repository;


import com.yeongjun.yeongjun.transactions.model.TransactionsCategory;
import com.yeongjun.yeongjun.global.repository.BaseDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionsCategoryDAO extends BaseDAO<TransactionsCategory> {
    @Override
    protected String getNamespace() {
        return "transactions.category";
    }

    // 전체 카테고리 조회
    public List<TransactionsCategory> selectAllTransactionsCategory() {
        return selectList("selectAllTransactionCategories", null);
    }

    // 수입 카테고리 조회
    public List<TransactionsCategory> selectIncomeCategories() {
        return selectList("selectIncomeCategories", null);
    }

    // 지출 카테고리 조회
    public List<TransactionsCategory> selectExpenseCategories() {
        return selectList("selectExpenseCategories", null);
    }
}
