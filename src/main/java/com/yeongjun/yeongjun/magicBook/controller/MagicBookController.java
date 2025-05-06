package com.yeongjun.yeongjun.magicBook.controller;

import com.yeongjun.yeongjun.magicBook.entity.MagicBookEntry;
import com.yeongjun.yeongjun.magicBook.repository.MagicBookDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/magicBook")
public class MagicBookController {

    private final MagicBookDAO magicBookDAO;

    public MagicBookController(MagicBookDAO magicBookDAO) {
        this.magicBookDAO = magicBookDAO;
    }

    @GetMapping("/sports")
    public String sports(Model model) {
        addCategoryData("sports", model);
        return "magicBook/sports";
    }

    @GetMapping("/work")
    public String work(Model model) {
        addCategoryData("work", model);
        return "magicBook/work";
    }

    @GetMapping("/meal")
    public String meal(Model model) {
        addCategoryData("meal", model);
        return "magicBook/meal";
    }

    @GetMapping("/relations")
    public String relations(Model model) {
        addCategoryData("relations", model);
        return "magicBook/relations";
    }

    @GetMapping("/whatever")
    public String whatever(Model model) {
        addCategoryData("whatever", model);
        return "magicBook/whatever";
    }

    @GetMapping("/todayHoroscope")
    public String todayHoroscope(Model model) {
        addCategoryData("todayHoroscope", model);
        return "magicBook/todayHoroscope";
    }

    /**
     * 카테고리 데이터(Model) 세팅 헬퍼 메서드
     */
    private void addCategoryData(String category, Model model) {
        List<MagicBookEntry> list = magicBookDAO.selectAllByCategory(category);
        model.addAttribute("Entries", list);
    }
}
