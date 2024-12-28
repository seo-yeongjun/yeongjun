package com.yeongjun.yeongjun.transactions.repository;

import com.yeongjun.yeongjun.global.repository.BaseDAO;
import com.yeongjun.yeongjun.transactions.dto.TransactionBetween;
import com.yeongjun.yeongjun.transactions.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TransactionsDAO extends BaseDAO<Transaction> {
    @Override
    protected String getNamespace() {
        return "transactions.record";
    }

    // 사용자별 수입 거래 조회
    public List<Transaction> selectIncomeTransactionsByUser(String username) {
        return selectList("selectIncomeTransactionsByUser", username);
    }

    // 사용자별 지출 거래 조회
    public List<Transaction> selectExpenseTransactionsByUser(String username) {
        return selectList("selectExpenseTransactionsByUser", username);
    }

    // 사용자별 전체 거래 조회
    public List<Transaction> selectAllTransactionsByUser(String username) {
        return selectList("selectAllTransactionsByUser", username);
    }

    // 사용자별 전체 거래 조회
    public List<Transaction> selectAllTransactionsByUserAndCreatedDate(Object params) {
        return selectList("selectAllTransactionsByUserAndCreatedDate", params);
    }

    // 사용자, 연도-월, 카테고리별 수입 거래 조회
    public List<Transaction> selectIncomeTransactionsByUserAndYearMonthCategory(Object params) {
        return selectList("selectIncomeTransactionsByUserAndYearMonthCategory", params);
    }

    // 사용자, 연도-월, 카테고리별 지출 거래 조회
    public List<Transaction> selectExpenseTransactionsByUserAndYearMonthCategory(Object params) {
        return selectList("selectExpenseTransactionsByUserAndYearMonthCategory", params);
    }

    //사용자, 연도-월, 지출수입조회
    public List<Transaction> selectAllTransactionsByUserAndTransactionMonth(Transaction params) {
        return selectList("selectAllTransactionsByUserAndTransactionMonth", params);
    }

    // 거래 추가
    public void insertTransaction(Transaction transaction) {
        insert("insertTransactions", transaction);
    }

    // 거래 수정
    public void updateTransaction(Transaction transaction) {
        update("updateTransactions", transaction);
    }

    public List<Transaction> selectTransactionsBetweenDates(TransactionBetween params) {
        return selectList("selectTransactionsBetweenDates", params);
    }

    public boolean deleteTransaction(Transaction params) {
        return delete("deleteTransaction", params)>0;
    }
}
