package com.yeongjun.yeongjun.home.widget.controller;

import com.yeongjun.yeongjun.Security.model.Role;
import com.yeongjun.yeongjun.Security.model.User;
import com.yeongjun.yeongjun.home.widget.model.BalanceGame;
import com.yeongjun.yeongjun.home.widget.model.BalanceGameVote;
import com.yeongjun.yeongjun.home.widget.model.WidgetHoliday;
import com.yeongjun.yeongjun.home.widget.service.WidgetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/widgets")
@Slf4j
public class WidgetController {

    private final WidgetService widgetService;

    public WidgetController(WidgetService widgetService) {
        this.widgetService = widgetService;
    }

    // 1. 퇴근 시간 카운트다운 정보 조회
    @GetMapping("/countdown")
    public ResponseEntity<Map<String, Object>> getCountdown() {
        return ResponseEntity.ok(widgetService.getCountdownData());
    }

    // 2. 퇴근 카운트다운 설정 변경 (ADMIN 전용)
    @PostMapping("/countdown/config")
    public ResponseEntity<?> updateCountdownConfig(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> configs) {
        
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한이 필요합니다.");
        }

        try {
            widgetService.updateCountdownConfig(configs);
            return ResponseEntity.ok("설정이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("퇴근 설정 저장 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("설정 저장 중 오류가 발생했습니다.");
        }
    }

    // 2.1 공휴일 목록 조회
    @GetMapping("/countdown/holidays")
    public ResponseEntity<List<WidgetHoliday>> getHolidays() {
        return ResponseEntity.ok(widgetService.getHolidayList());
    }

    // 2.2 공휴일 추가 (ADMIN 전용)
    @PostMapping("/countdown/holidays")
    public ResponseEntity<?> addHoliday(
            @AuthenticationPrincipal User user,
            @RequestBody WidgetHoliday holiday) {
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한이 필요합니다.");
        }
        try {
            widgetService.addHoliday(holiday);
            return ResponseEntity.ok("공휴일이 추가되었습니다.");
        } catch (Exception e) {
            log.error("공휴일 추가 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("공휴일 추가 중 오류가 발생했습니다.");
        }
    }

    // 2.3 공휴일 삭제 (ADMIN 전용)
    @DeleteMapping("/countdown/holidays")
    public ResponseEntity<?> deleteHoliday(
            @AuthenticationPrincipal User user,
            @RequestParam("date") String dateStr) {
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한이 필요합니다.");
        }
        try {
            LocalDate date = LocalDate.parse(dateStr);
            widgetService.deleteHoliday(date);
            return ResponseEntity.ok("공휴일이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("공휴일 삭제 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("공휴일 삭제 중 오류가 발생했습니다.");
        }
    }

    // 3. 실시간 날씨 조회 (성공회대 기준)
    @GetMapping("/weather")
    public ResponseEntity<WidgetService.WeatherForecast> getWeather() {
        return ResponseEntity.ok(widgetService.getWeatherData());
    }

    // 4. 대중교통 실시간 도착 정보 조회
    @GetMapping("/transit")
    public ResponseEntity<WidgetService.TransitForecast> getTransit() {
        return ResponseEntity.ok(widgetService.getTransitData());
    }

    // 5. 전체 밸런스 게임 목록 조회
    @GetMapping("/balance-game")
    public ResponseEntity<List<BalanceGame>> getBalanceGames() {
        List<BalanceGame> games = widgetService.getAllBalanceGames();
        if (games.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(games);
    }

    // 6. 밸런스 게임 투표 (IP 기반)
    @PostMapping("/balance-game/vote")
    public ResponseEntity<Map<String, Object>> voteBalanceGame(
            HttpServletRequest request,
            @RequestBody BalanceGameVote voteDto) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            String ipAddress = getClientIp(request);
            result = widgetService.voteBalanceGame(voteDto.getQuestionId(), voteDto.getSelection(), ipAddress);
            if (Boolean.TRUE.equals(result.get("success"))) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("밸런스 게임 투표 중 오류 발생: ", e);
            result.put("success", false);
            result.put("message", "서버 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // 7. 신규 밸런스 게임 등록 (ADMIN 전용)
    @PostMapping("/balance-game/create")
    public ResponseEntity<?> createBalanceGame(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {
        
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("관리자 권한이 필요합니다.");
        }

        String question = body.get("question");
        String optionA = body.get("optionA");
        String optionB = body.get("optionB");

        if (question == null || question.trim().isEmpty() ||
            optionA == null || optionA.trim().isEmpty() ||
            optionB == null || optionB.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("질문과 선택지 A, B를 모두 입력해 주세요.");
        }

        BalanceGame newGame = new BalanceGame();
        newGame.setQuestion(question);
        newGame.setOptionA(optionA);
        newGame.setOptionB(optionB);

        boolean success = widgetService.createBalanceGame(newGame);
        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "새로운 밸런스 게임이 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게임 등록 중 오류가 발생했습니다.");
        }
    }

    // 사용자 실제 IP 획득 메서드
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For에 프록시 거쳐 여러 IP가 찍힐 경우 첫번째 IP가 실제 클라이언트 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
