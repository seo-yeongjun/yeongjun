package com.yeongjun.yeongjun.coffeeGame.controller;

import com.yeongjun.yeongjun.coffeeGame.entity.CoffeeGameRecord;
import com.yeongjun.yeongjun.coffeeGame.entity.LoseRateStat;
import com.yeongjun.yeongjun.coffeeGame.entity.CostStat;
import com.yeongjun.yeongjun.coffeeGame.repository.CoffeeGameStatDAO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        
        List<CoffeeGameRecord> records = coffeeGameStatDAO.getAllRecords();
        
        // 패배 비율 통계 계산
        Map<String, Integer> loseCountMap = records.stream()
                .collect(Collectors.groupingBy(CoffeeGameRecord::getLoser_name, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        
        int totalGames = records.size();
        List<LoseRateStat> loseRateStats = loseCountMap.entrySet().stream()
                .map(entry -> new LoseRateStat(
                        entry.getKey(),
                        entry.getValue(),
                        (double) entry.getValue() / totalGames * 100
                ))
                .collect(Collectors.toList());
        
        // 금액 총합 통계 계산
        Map<String, Integer> costMap = records.stream()
                .collect(Collectors.groupingBy(CoffeeGameRecord::getLoser_name, 
                        Collectors.summingInt(CoffeeGameRecord::getCost)));
        
        List<CostStat> costStats = costMap.entrySet().stream()
                .map(entry -> new CostStat(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        model.addAttribute("loseRateStats", loseRateStats);
        model.addAttribute("costStats", costStats);
        
        return "coffeeGame/gogoClubStat/stats";
    }
}