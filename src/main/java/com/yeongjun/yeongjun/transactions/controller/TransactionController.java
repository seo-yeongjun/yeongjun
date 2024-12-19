package com.yeongjun.yeongjun.transactions.controller;

import com.yeongjun.yeongjun.Security.model.User;
import com.yeongjun.yeongjun.transactions.TransactionRequest;
import com.yeongjun.yeongjun.transactions.model.Transaction;
import com.yeongjun.yeongjun.transactions.repository.TransactionsDAO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

        Transaction params = new Transaction();
        params.setUsername(user.getUsername());
        params.setCreated_at(LocalDateTime.now());
        model.addAttribute("transactionsListToday", transactionsDAO.selectAllTransactionsByUserAndCreatedDate(params));
        model.addAttribute("transaction", new TransactionRequest());
        return "transactions/record";
    }

    // 트랜잭션 추가를 처리하는 POST 요청
    @PostMapping("add")
    public String addTransaction(@AuthenticationPrincipal User user,@Valid @ModelAttribute("transaction") TransactionRequest transaction,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            Transaction params = new Transaction();
            params.setUsername(user.getUsername());
            params.setCreated_at(LocalDateTime.now());
            model.addAttribute("transactionsListToday", transactionsDAO.selectAllTransactionsByUserAndCreatedDate(params));
            return "transactions/record"; // 다시 폼으로 돌아감
        }
        Transaction newTransaction = new Transaction();
        newTransaction.setUsername(user.getUsername());
        newTransaction.setCreated_at(LocalDateTime.now());
        newTransaction.setAmount(transaction.getAmount());
        newTransaction.setCategory_id(transaction.getCategory_id());
        newTransaction.setMemo(transaction.getMemo());
        newTransaction.setTransaction_date(Date.valueOf(transaction.getTransaction_date()));
        transactionsDAO.insertTransaction(newTransaction);
        return "redirect:/transactions/record"; // 트랜잭션 목록 페이지로 리다이렉트
    }
}