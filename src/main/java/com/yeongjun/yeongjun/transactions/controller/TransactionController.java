package com.yeongjun.yeongjun.transactions.controller;

import com.yeongjun.yeongjun.Security.model.User;
import com.yeongjun.yeongjun.transactions.repository.TransactionsDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/transactions") // 클래스 수준에서 /transaction 경로 지정
@Slf4j
@PreAuthorize("hasAnyAuthority('ADMIN', 'HYERIN', 'USER')")
public class TransactionController {

    private final TransactionsDAO transactionsDAO;

    public TransactionController(TransactionsDAO transactionsDAO) {
        this.transactionsDAO = transactionsDAO;
    }


    @GetMapping({"", "/"})
    public String transactionsPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("title", "가계부");
        return "transactions/home";
    }

    @GetMapping("record")
    public String transactionsRecordPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("title", "가계부-소비기록\uD83D\uDCDA");
        model.addAttribute("transactionsList", transactionsDAO.selectAllTransactionsByUser(user.getUsername()));
        return "transactions/record";
    }
}