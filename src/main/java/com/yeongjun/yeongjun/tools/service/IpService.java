package com.yeongjun.yeongjun.tools.service;

import io.ipinfo.api.IPinfo;
import io.ipinfo.api.errors.RateLimitedException;
import io.ipinfo.api.model.IPResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class IpService {

    public IPResponse getIpInfo(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");

        if (ip != null && !ip.isEmpty()) {
            // 1. 여러 개의 IP가 있을 경우 첫 번째 IP만 추출
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
        } else {
            // 2. 헤더가 없을 경우에만 getRemoteAddr() 호출
            ip = request.getRemoteAddr();
        }

        IPinfo ipInfo = new IPinfo.Builder()
                .setToken("aa11a38955e02c")
                .build();

        try {
           return ipInfo.lookupIP(ip);
        } catch (RateLimitedException ex) {
            return null;
        }
    }
}
