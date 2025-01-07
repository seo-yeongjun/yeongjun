package com.yeongjun.yeongjun.tools.controller;

import com.yeongjun.yeongjun.tools.service.IpService;
import io.ipinfo.api.model.IPResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tools")
public class ToolsController {

    private final IpService ipService;

    public ToolsController(IpService ipService) {
        this.ipService = ipService;
    }


    @GetMapping({"", "/"})
    public String toolsHome() {
        return "tools/home";
    }

    @GetMapping("myIp")
    public String myIp(HttpServletRequest request, Model model) {
        IPResponse ipResponse = ipService.getIpInfo(request);

        model.addAttribute("ipInfo", ipResponse);

        return "tools/myIp";
    }

    @GetMapping("letterCount")
    public String letterCount() {
        return "tools/letterCount";
    }

    @GetMapping("textCompare")
    public String textCompare() {return "tools/textCompare";}
}
