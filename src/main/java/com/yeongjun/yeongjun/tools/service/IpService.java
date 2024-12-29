package com.yeongjun.yeongjun.tools.service;

import com.yeongjun.yeongjun.tools.model.IpLocation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IpService {

    public boolean isPrivateIP(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        ip = ip.toLowerCase();

        // 1) IPv6 localhost (::1, 0:0:0:0:0:0:0:1)
        if (ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1")) {
            return true; // loopback -> 내부 IP
        }

        // 2) IPv4 localhost (127.0.0.1)
        if (ip.equals("127.0.0.1")) {
            return true; // loopback -> 내부 IP
        }

        //IPv6 사설 체크
        if (ip.startsWith("fc") || ip.startsWith("fd")  // unique local
                || ip.startsWith("fe80")) {            // link-local
            return true;
        }

        // 4) IPv4 사설 체크
        if (ip.matches("^10\\.\\d+\\.\\d+\\.\\d+$")) {
            return true;
        }
        if (ip.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\.\\d+\\.\\d+$")) {
            return true;
        }
        if (ip.matches("^192\\.168\\.\\d+\\.\\d+$")) {
            return true;
        }
        // 링크 로컬(IPv4) 169.254.x.x
        if (ip.matches("^169\\.254\\.\\d+\\.\\d+$")) {
            return true;
        }

        // 그 외에는 외부 IP로 봄
        return false;
    }

    public IpLocation getIpLocation(String ip) {
        String url = "http://ip-api.com/json/" + ip + "?fields=country,regionName,city,isp,status,message";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<IpLocation> response = restTemplate.getForEntity(url, IpLocation.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            IpLocation location = response.getBody();
            return location;
        }
        return null;
    }

}
