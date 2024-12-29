package com.yeongjun.yeongjun.tools.controller;

import com.yeongjun.yeongjun.tools.model.IpLocation;
import com.yeongjun.yeongjun.tools.service.IpService;
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
        // 1) IP 얻기
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        // 2) 내부 IP 여부 판별
        boolean isPrivate = ipService.isPrivateIP(ip);

        model.addAttribute("ip", ip);

        if (isPrivate) {
            return "tools/myIp";
        }
        // 4) 외부 IP면 위치 정보 조회 등 처리
        IpLocation location = ipService.getIpLocation(ip);
        model.addAttribute("location", location);

        return "tools/myIp";
    }

}
