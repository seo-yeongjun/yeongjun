package com.yeongjun.yeongjun.ajeGag.controller;

import com.yeongjun.yeongjun.ajeGag.model.Ajegag;
import com.yeongjun.yeongjun.ajeGag.repository.AjegagDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/ajeGag")
@Slf4j
public class AjeGagController {

    private final AjegagDAO ajeGagDAO;

    public AjeGagController(AjegagDAO ajeGagDAO) {
        this.ajeGagDAO = ajeGagDAO;
    }

    @GetMapping({"", "/"})
    public String getNewsBySearch(Model model) {
        return "ajeGag/home";
    }

    @GetMapping("/getList")
    @ResponseBody
    public List<Ajegag> getList() {
        return ajeGagDAO.selectAjegagList();
    }

    @GetMapping("/getHumor")
    @ResponseBody
    public Ajegag getHumor(@RequestParam("id") Long id) {
        Ajegag ajegag = ajeGagDAO.findById(id);
        return ajegag != null ? ajegag : new Ajegag();
    }


}