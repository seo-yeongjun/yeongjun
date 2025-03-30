package com.yeongjun.yeongjun.coffeeGame.controller;

import com.yeongjun.yeongjun.coffeeGame.entity.CoffeeGameRecord;
import com.yeongjun.yeongjun.coffeeGame.repository.CoffeeGameStatDAO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/coffeeGame/gogoClubStat")
@Slf4j
public class GogoClubStatController {

    private final CoffeeGameStatDAO coffeeGameStatDAO;
    private static final String PASSWORD = "gogo";

    public GogoClubStatController(CoffeeGameStatDAO coffeeGameStatDAO) {
        this.coffeeGameStatDAO = coffeeGameStatDAO;
    }

    @GetMapping("")
    public String loginPage() {
        return "coffeeGame/gogoClubStat/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String password, HttpSession session) {
        if (PASSWORD.equals(password)) {
            session.setAttribute("authenticated", true);
            return "redirect:/coffeeGame/gogoClubStat/record";
        }
        return "redirect:/coffeeGame/gogoClubStat?error=true";
    }

    @GetMapping("/record")
    public String record(HttpSession session, Model model) {
        if (session.getAttribute("authenticated") == null) {
            return "redirect:/coffeeGame/gogoClubStat";
        }
        List<CoffeeGameRecord> records = coffeeGameStatDAO.getAllRecords();
        model.addAttribute("records", records);
        return "coffeeGame/gogoClubStat/record";
    }

    @PostMapping("/record")
    public String saveRecord(@ModelAttribute CoffeeGameRecord record, HttpSession session) {
        if (session.getAttribute("authenticated") == null) {
            return "redirect:/coffeeGame/gogoClubStat";
        }
        record.setDate(LocalDate.now());
        coffeeGameStatDAO.saveRecord(record);
        return "redirect:/coffeeGame/gogoClubStat/record";
    }

    @GetMapping("/stats")
    public String stats(HttpSession session, Model model) {
        if (session.getAttribute("authenticated") == null) {
            return "redirect:/coffeeGame/gogoClubStat";
        }
        // 통계 데이터를 모델에 추가
        model.addAttribute("loserStats", coffeeGameStatDAO.getAllRecords().stream()
                .map(record -> new Object[]{
                        record.getLoser_name(),
                        coffeeGameStatDAO.getTotalCostByLoser(record.getLoser_name())
                })
                .distinct()
                .toList());

        return "coffeeGame/gogoClubStat/stats";
    }
}