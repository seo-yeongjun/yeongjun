package com.yeongjun.yeongjun.transactions.controller;

import com.yeongjun.yeongjun.Security.model.User;
import com.yeongjun.yeongjun.transactions.TransactionRequest;
import com.yeongjun.yeongjun.transactions.dto.DayTransactions;
import com.yeongjun.yeongjun.transactions.model.Transaction;
import com.yeongjun.yeongjun.transactions.repository.TransactionsDAO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transactions") // 클래스 수준에서 /transaction 경로 지정
@Slf4j
@PreAuthorize("hasAnyAuthority('ADMIN', 'HYERIN', 'USER')")
public class TransactionController {

    private final TransactionsDAO transactionsDAO;

    public TransactionController(TransactionsDAO transactionsDAO) {
        this.transactionsDAO = transactionsDAO;
    }

    @GetMapping({"", "/", "record"})
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
    public String addTransaction(@AuthenticationPrincipal User user, @Valid @ModelAttribute("transaction") TransactionRequest transaction,
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

    @GetMapping("calendar")
    public String getCalendar(
            @AuthenticationPrincipal User user,
            Model model,
            @RequestParam(required = false, defaultValue = "") String yearParam,
            @RequestParam(required = false, defaultValue = "") String monthParam) {

        model.addAttribute("title", "소비달력\uD83D\uDCB0\uD83D\uDCC5");

        LocalDate today = LocalDate.now();
        int year = yearParam.isEmpty() ? today.getYear() : Integer.parseInt(yearParam);
        int month = monthParam.isEmpty() ? today.getMonthValue() : Integer.parseInt(monthParam);

        // 1) 특정 월(yyyy-MM)의 모든 트랜잭션
        Transaction params = new Transaction();
        params.setUsername(user.getUsername());
        params.setTransaction_date(Date.valueOf(LocalDate.of(year, month, 1)));

        List<Transaction> transactions =
                transactionsDAO.selectAllTransactionsByUserAndTransactionMonth(params);

        // 2) 날짜별로 트랜잭션 그룹핑
        Map<Integer, List<Transaction>> dayMap = transactions.stream()
                .collect(Collectors.groupingBy(tx -> tx.getTransaction_date()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .getDayOfMonth()));

        // 3) 이번 달 날짜 범위
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        int lengthOfMonth = firstDayOfMonth.lengthOfMonth();
        int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue(); // 월=1 ~ 일=7 (ISO-8601)

        // 4) 1차원 dayTxList 생성 (같은 로직)
        List<DayTransactions> dayTxList = new ArrayList<>();
        for (int d = 1; d <= lengthOfMonth; d++) {
            DayTransactions dto = new DayTransactions();
            dto.setYear(year);
            dto.setMonth(month);
            dto.setDay(d);

            List<Transaction> dayTx = dayMap.get(d); // 해당 날짜 트랜잭션
            if (dayTx != null) {
                // 누적합 계산
                int plusSum = 0;
                int minusSum = 0;

                for (Transaction tx : dayTx) {
                    if (tx.getIncome_expense_gb() == 1) {
                        // 수입
                        plusSum += tx.getAmount();
                    } else if (tx.getIncome_expense_gb() == 2) {
                        // 소비
                        minusSum += tx.getAmount();
                    }
                }

                // dto에 저장
                dto.setSumOfPlusAmounts(plusSum);
                dto.setSumOfMinusAmounts(minusSum);
                dto.setTransactions(dayTx);
            }

            dayTxList.add(dto);
        }

        // 5) 2차원 리스트(weeks) 만들기
        List<List<DayTransactions>> weeks = new ArrayList<>();
        List<DayTransactions> currentWeek = new ArrayList<>();

        // 5-1) 첫 주의 시작 요일 전까지 빈 칸(null) 채우기
        // (예) startDayOfWeek=3(수요일) -> 월(1),화(2) 칸은 null
        for (int i = 1; i < startDayOfWeek; i++) {
            currentWeek.add(null);
        }

        // 5-2) 날짜를 currentWeek에 채우다가 7칸 되면 weeks에 add
        for (DayTransactions dt : dayTxList) {
            currentWeek.add(dt);
            if (currentWeek.size() == 7) {
                weeks.add(currentWeek);
                currentWeek = new ArrayList<>();
            }
        }

        // 5-3) 마지막 주가 7칸 미만이면 남은 칸 null로 채워주기
        if (!currentWeek.isEmpty()) {
            while (currentWeek.size() < 7) {
                currentWeek.add(null);
            }
            weeks.add(currentWeek);
        }

        // 모델에 담기
        model.addAttribute("weeks", weeks);
        model.addAttribute("year", year);
        model.addAttribute("month", month);

        return "transactions/calendar";
    }

}