package com.yeongjun.yeongjun.magicBook.controller;

import com.yeongjun.yeongjun.magicBook.entity.MagicBookEntry;
import com.yeongjun.yeongjun.magicBook.repository.MagicBookDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

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
    public String sports(
            @RequestParam(value = "id", required = false) Long id,
            Model model) {
        addCategoryData("sports", id, model);
        return "magicBook/sports";
    }

    @GetMapping("/work")
    public String work(
            @RequestParam(value = "id", required = false) Long id,
            Model model) {
        addCategoryData("work", id, model);
        return "magicBook/work";
    }

    @GetMapping("/meal")
    public String meal(
            @RequestParam(value = "id", required = false) Long id,
            Model model) {
        addCategoryData("meal", id, model);
        return "magicBook/meal";
    }

    @GetMapping("/relations")
    public String relations(
            @RequestParam(value = "id", required = false) Long id,
            Model model) {
        addCategoryData("relations", id, model);
        return "magicBook/relations";
    }

    @GetMapping("/whatever")
    public String whatever(
            @RequestParam(value = "id", required = false) Long id,
            Model model) {
        addCategoryData("whatever", id, model);
        return "magicBook/whatever";
    }

    @GetMapping("/todayHoroscope")
    public String todayHoroscope(
            @RequestParam(value = "id", required = false) Long id,
            Model model) {
        addCategoryData("todayHoroscope", id, model);
        return "magicBook/todayHoroscope";
    }

    /**
     * 카테고리 데이터(Model) 세팅 헬퍼 메서드
     */
    private void addCategoryData(String category, Long id, Model model) {
        if (id != null) {
            MagicBookEntry entry = magicBookDAO.selectById(id);
            if (entry == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            model.addAttribute("answer", entry.getAnswer());
        }
        List<MagicBookEntry> list = magicBookDAO.selectAllByCategory(category);
        model.addAttribute(category + "Entries", list);
    }
}
