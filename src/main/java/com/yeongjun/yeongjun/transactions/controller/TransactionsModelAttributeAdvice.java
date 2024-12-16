package com.yeongjun.yeongjun.transactions.controller;

import com.yeongjun.yeongjun.global.service.CategoryService;
import com.yeongjun.yeongjun.transactions.repository.TransactionsCategoryDAO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class TransactionsModelAttributeAdvice {

    private final CategoryService categoryService;
    private final TransactionsCategoryDAO transactionsCategoryDAO;

    public TransactionsModelAttributeAdvice(CategoryService categoryService, TransactionsCategoryDAO transactionsCategoryDAO) {
        this.categoryService = categoryService;
        this.transactionsCategoryDAO = transactionsCategoryDAO;
    }

    @ModelAttribute
    public void transactionsAttributes(HttpServletRequest request, Model model) {
        if (request.getRequestURI().startsWith("/transactions")) {
            model.addAttribute("transactionsCategory", categoryService.getTransactionsCategoryList());
            model.addAttribute("transactionsRecordIncomeCategory", transactionsCategoryDAO.selectIncomeCategories());
            model.addAttribute("transactionsRecordExpenseCategory", transactionsCategoryDAO.selectExpenseCategories());
            model.addAttribute("transactionsRecordAllCategory", transactionsCategoryDAO.selectAllTransactionsCategory());
        }
    }

}