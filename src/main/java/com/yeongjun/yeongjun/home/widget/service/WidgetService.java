package com.yeongjun.yeongjun.home.widget.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeongjun.yeongjun.home.widget.model.BalanceGame;
import com.yeongjun.yeongjun.home.widget.model.BalanceGameVote;
import com.yeongjun.yeongjun.home.widget.model.WidgetConfig;
import com.yeongjun.yeongjun.home.widget.model.WidgetHoliday;
import com.yeongjun.yeongjun.home.widget.repository.WidgetDAO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class WidgetService {

    private final WidgetDAO widgetDAO;

    @Value("${weather.api-key:}")
    private String weatherApiKey;



    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 캐시 저장소
    private WeatherForecast cachedWeather = null;
    private LocalDateTime weatherCacheTime = null;



    public WidgetService(WidgetDAO widgetDAO) {
        this.widgetDAO = widgetDAO;
    }

    @PostConstruct
    public void initDatabaseSchema() {
        try {
            widgetDAO.alterConfigValueColumnType();
            log.info("성공적으로 widget_config 테이블의 config_value 컬럼을 TEXT 타입으로 확장하였습니다.");
        } catch (Exception e) {
            log.warn("widget_config 테이블 컬럼 확장 중 예외 발생 (이미 적용되었거나 권한 부족 가능성): {}", e.getMessage());
        }
    }

    // ==========================================
    // 1. 퇴근 시간 카운트다운 서비스
    // ==========================================
    private boolean isWorkingDay(LocalDate date, Set<LocalDate> holidayDates) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        return !holidayDates.contains(date);
    }

    public Map<String, Object> getCountdownData() {
        Map<String, Object> result = new HashMap<>();

        // 현재 시각 정보
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 1) 퇴근 시각 구하기
        // 방학 여부에 따른 퇴근 시간 분기
        String leaveTimeStr = widgetDAO.selectConfig("OFFICE_LEAVE_TIME_NORMAL");
        String vacationStartStr = widgetDAO.selectConfig("VACATION_START_DATE");
        String semesterStartStr = widgetDAO.selectConfig("SEMESTER_START_DATE");
        
        boolean isVacation = false;
        try {
            if (vacationStartStr != null && semesterStartStr != null) {
                LocalDate vacStart = LocalDate.parse(vacationStartStr);
                LocalDate semStart = LocalDate.parse(semesterStartStr);
                if (today.isAfter(vacStart) && today.isBefore(semStart)) {
                    leaveTimeStr = widgetDAO.selectConfig("OFFICE_LEAVE_TIME_VACATION");
                    isVacation = true;
                }
            }
        } catch (Exception e) {
            log.error("방학 기간 판단 중 에러 발생: ", e);
        }

        if (leaveTimeStr == null) {
            leaveTimeStr = "18:00:00"; // 기본값
        }
        
        LocalTime leaveTime = LocalTime.parse(leaveTimeStr);
        LocalDateTime todayLeaveDateTime = LocalDateTime.of(today, leaveTime);

        // 2) 공휴일 목록 조회
        List<WidgetHoliday> holidays = widgetDAO.selectHolidayList();
        Set<LocalDate> holidayDates = new HashSet<>();
        if (holidays != null) {
            for (WidgetHoliday h : holidays) {
                holidayDates.add(h.getHolidayDate());
            }
        }

        // 3) 출근/퇴근 카운트다운 계산
        boolean isWorkCountdown = true;
        long secondsToEvent = 0;
        
        boolean todayIsWorking = isWorkingDay(today, holidayDates);
        LocalDateTime todayWorkStartDateTime = LocalDateTime.of(today, LocalTime.of(9, 0, 0));
        
        if (todayIsWorking) {
            if (now.isBefore(todayWorkStartDateTime)) {
                isWorkCountdown = true;
                secondsToEvent = ChronoUnit.SECONDS.between(now, todayWorkStartDateTime);
            } else if (now.isBefore(todayLeaveDateTime)) {
                isWorkCountdown = false;
                secondsToEvent = ChronoUnit.SECONDS.between(now, todayLeaveDateTime);
            } else {
                isWorkCountdown = true;
                // 다음 출근일 찾기
                LocalDate nextDate = today.plusDays(1);
                while (!isWorkingDay(nextDate, holidayDates)) {
                    nextDate = nextDate.plusDays(1);
                }
                LocalDateTime nextWorkStartDateTime = LocalDateTime.of(nextDate, LocalTime.of(9, 0, 0));
                secondsToEvent = ChronoUnit.SECONDS.between(now, nextWorkStartDateTime);
            }
        } else {
            isWorkCountdown = true;
            // 다음 출근일 찾기
            LocalDate nextDate = today.plusDays(1);
            while (!isWorkingDay(nextDate, holidayDates)) {
                nextDate = nextDate.plusDays(1);
            }
            LocalDateTime nextWorkStartDateTime = LocalDateTime.of(nextDate, LocalTime.of(9, 0, 0));
            secondsToEvent = ChronoUnit.SECONDS.between(now, nextWorkStartDateTime);
        }

        // 4) 이번 달 남은 출근일 계산 (주말 및 공휴일 제외)
        int remainingWorkingDays = 0;
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        for (LocalDate date = today; !date.isAfter(lastDayOfMonth); date = date.plusDays(1)) {
            if (isWorkingDay(date, holidayDates)) {
                remainingWorkingDays++;
            }
        }

        // 5) 다음 쉬는날 찾기 및 D-day 계산
        LocalDate restDayDate = today;
        while (isWorkingDay(restDayDate, holidayDates)) {
            restDayDate = restDayDate.plusDays(1);
        }
        long restDayDday = ChronoUnit.DAYS.between(today, restDayDate);
        String nextRestDayName = "주말";
        if (holidayDates.contains(restDayDate)) {
            for (WidgetHoliday h : holidays) {
                if (h.getHolidayDate().equals(restDayDate)) {
                    nextRestDayName = h.getHolidayName();
                    break;
                }
            }
        }

        // 6) 종강/학기 시작 D-day 계산
        long vacationDday = -1;
        String dDayLabel = "종강까지";
        try {
            if (vacationStartStr != null && semesterStartStr != null) {
                LocalDate vacStart = LocalDate.parse(vacationStartStr);
                LocalDate semStart = LocalDate.parse(semesterStartStr);

                if (today.isBefore(vacStart)) {
                    vacationDday = ChronoUnit.DAYS.between(today, vacStart);
                    dDayLabel = "종강까지";
                } else if (today.isBefore(semStart)) {
                    vacationDday = ChronoUnit.DAYS.between(today, semStart);
                    dDayLabel = "학기시작까지";
                } else {
                    vacationDday = 0;
                    dDayLabel = "종강까지";
                }
            }
        } catch (Exception e) {
            log.error("종강 D-day 계산 에러: ", e);
        }

        result.put("isVacation", isVacation);
        result.put("leaveTime", leaveTimeStr);
        result.put("isWorkCountdown", isWorkCountdown);
        result.put("secondsToEvent", secondsToEvent);
        result.put("remainingWorkingDays", remainingWorkingDays);
        result.put("nextRestDayName", nextRestDayName);
        result.put("restDayDday", restDayDday);
        result.put("vacationDday", vacationDday);
        result.put("dDayLabel", dDayLabel);
        
        String leaveTimeNormal = widgetDAO.selectConfig("OFFICE_LEAVE_TIME_NORMAL");
        String leaveTimeVacation = widgetDAO.selectConfig("OFFICE_LEAVE_TIME_VACATION");
        result.put("leaveTimeNormal", leaveTimeNormal != null ? leaveTimeNormal : "18:00:00");
        result.put("leaveTimeVacation", leaveTimeVacation != null ? leaveTimeVacation : "16:00:00");
        result.put("vacationStartDate", vacationStartStr != null ? vacationStartStr : "");
        result.put("semesterStartDate", semesterStartStr != null ? semesterStartStr : "");

        return result;
    }

    @Transactional
    public void updateCountdownConfig(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            WidgetConfig config = new WidgetConfig();
            config.setConfigKey(entry.getKey());
            config.setConfigValue(entry.getValue());
            widgetDAO.updateConfig(config);
        }
    }

    public List<WidgetHoliday> getHolidayList() {
        return widgetDAO.selectHolidayList();
    }

    @Transactional
    public void addHoliday(WidgetHoliday holiday) {
        widgetDAO.insertHoliday(holiday);
    }

    @Transactional
    public void deleteHoliday(LocalDate date) {
        widgetDAO.deleteHoliday(date);
    }

    // ==========================================
    // 2. 날씨 서비스 (성공회대학교 기준, API 폴백)
    // ==========================================
    // 예보 발표 기준 시각과 날짜 구하기 (15분 마진)
    private String[] getBaseDateTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.minusMinutes(15);
        
        LocalDate date = target.toLocalDate();
        LocalTime time = target.toLocalTime();
        
        String baseDate = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = "0200";
        
        int hour = time.getHour();
        if (hour < 2) {
            LocalDate yesterday = date.minusDays(1);
            baseDate = yesterday.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "2300";
        } else if (hour < 5) {
            baseTime = "0200";
        } else if (hour < 8) {
            baseTime = "0500";
        } else if (hour < 11) {
            baseTime = "0800";
        } else if (hour < 14) {
            baseTime = "1100";
        } else if (hour < 17) {
            baseTime = "1400";
        } else if (hour < 20) {
            baseTime = "1700";
        } else if (hour < 23) {
            baseTime = "2000";
        } else {
            baseTime = "2300";
        }
        
        return new String[]{baseDate, baseTime};
    }

    private String mapKmaWeatherStatus(int sky, int pty) {
        if (sky == 1 && pty == 0) {
            return "맑음";
        } else if ((sky == 3 || sky == 4) && pty == 0) {
            return "흐림"; // 구름조금 혹은 흐림
        } else if (pty == 1 || pty == 4) {
            return "비";
        } else if (pty == 2) {
            return "비/눈";
        } else if (pty == 3) {
            return "눈";
        }
        return "맑음";
    }

    public WeatherForecast getWeatherData() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1) 10초 인메모리 캐시 체크 (하루 9000회 한도 최적화)
        if (cachedWeather != null && weatherCacheTime != null && 
            ChronoUnit.SECONDS.between(weatherCacheTime, now) < 10) {
            return cachedWeather;
        }

        // 2) DB 캐시 체크
        String dbTimeStr = widgetDAO.selectConfig("WEATHER_CACHE_TIME");
        String dbDataStr = widgetDAO.selectConfig("WEATHER_CACHE_DATA");

        if (dbTimeStr != null && !dbTimeStr.trim().isEmpty() &&
            dbDataStr != null && !dbDataStr.trim().isEmpty()) {
            try {
                LocalDateTime dbTime = LocalDateTime.parse(dbTimeStr);
                if (ChronoUnit.SECONDS.between(dbTime, now) < 10) {
                    WeatherForecast dbForecast = objectMapper.readValue(dbDataStr, WeatherForecast.class);
                    // 인메모리 캐시 동기화
                    cachedWeather = dbForecast;
                    weatherCacheTime = dbTime;
                    return dbForecast;
                }
            } catch (Exception e) {
                log.warn("DB 날씨 캐시 파싱 중 예외 발생, 강제 갱신을 진행합니다: {}", e.getMessage());
            }
        }

        // 3) 캐시가 만료되었거나 없을 때 -> API 호출 진행
        WeatherForecast forecast = new WeatherForecast();
        forecast.setStation("성공회대학교(구로구 항동)");
        List<WeatherHour> hours = new ArrayList<>();
        boolean success = false;

        if (weatherApiKey != null && !weatherApiKey.trim().isEmpty()) {
            try {
                String[] base = getBaseDateTime();
                String baseDate = base[0];
                String baseTime = base[1];
                String serviceKey = weatherApiKey.trim();

                // 중복 URL Encoding 방지를 위한 URI 직접 생성
                String urlStr = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst"
                        + "?serviceKey=" + serviceKey
                        + "&pageNo=1"
                        + "&numOfRows=1000"
                        + "&dataType=JSON"
                        + "&base_date=" + baseDate
                        + "&base_time=" + baseTime
                        + "&nx=57"
                        + "&ny=125";
                java.net.URI uri = java.net.URI.create(urlStr);

                String res = restTemplate.getForObject(uri, String.class);
                JsonNode root = objectMapper.readTree(res);
                JsonNode responseNode = root.path("response");
                JsonNode headerNode = responseNode.path("header");
                String resultCode = headerNode.path("resultCode").asText();
                
                if (!"00".equals(resultCode)) {
                    throw new RuntimeException("기상청 API 에러: " + headerNode.path("resultMsg").asText());
                }

                JsonNode itemsNode = responseNode.path("body").path("items").path("item");
                if (itemsNode.isArray() && itemsNode.size() > 0) {
                    // 1) 현재 시각과 가장 일치하거나 근접한 미래 정각 데이터 찾기 (예: 현재 09:30 이면 10:00 예보)
                    LocalDateTime currentTarget = now.plusMinutes(30);
                    String currentFcstDate = currentTarget.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
                    String currentFcstTime = String.format("%02d00", currentTarget.getHour());

                    int currentTemp = 0;
                    int currentPop = 0;
                    int currentHumidity = 0;
                    double currentWindSpeed = 0.0;
                    int currentSky = 1;
                    int currentPty = 0;

                    for (JsonNode item : itemsNode) {
                        String fcstDate = item.path("fcstDate").asText();
                        String fcstTime = item.path("fcstTime").asText();
                        if (currentFcstDate.equals(fcstDate) && currentFcstTime.equals(fcstTime)) {
                            String category = item.path("category").asText();
                            String val = item.path("fcstValue").asText();
                            switch (category) {
                                case "TMP":
                                    try { currentTemp = (int) Math.round(Double.parseDouble(val)); } catch (Exception ignored) {}
                                    break;
                                case "POP":
                                    try { currentPop = (int) Math.round(Double.parseDouble(val)); } catch (Exception ignored) {}
                                    break;
                                case "REH":
                                    try { currentHumidity = Integer.parseInt(val); } catch (Exception ignored) {}
                                    break;
                                case "WSD":
                                    try { currentWindSpeed = Double.parseDouble(val); } catch (Exception ignored) {}
                                    break;
                                case "SKY":
                                    try { currentSky = Integer.parseInt(val); } catch (Exception ignored) {}
                                    break;
                                case "PTY":
                                    try { currentPty = Integer.parseInt(val); } catch (Exception ignored) {}
                                    break;
                            }
                        }
                    }

                    String status = mapKmaWeatherStatus(currentSky, currentPty);
                    forecast.setCurrentTemp(currentTemp);
                    forecast.setCurrentStatus(status);
                    forecast.setCurrentIcon(getWeatherIcon(status));
                    forecast.setCurrentPop(currentPop);
                    forecast.setCurrentHumidity(currentHumidity);
                    forecast.setCurrentWindSpeed(currentWindSpeed);

                    // 2) 오늘 하루 기온(TMP) 목록 수집 (최저/최고 기온 TMN, TMX 폴백용 및 실제 TMN/TMX 추출)
                    List<Integer> todayTemps = new ArrayList<>();
                    Integer tmn = null;
                    Integer tmx = null;
                    String todayStr = now.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

                    for (JsonNode item : itemsNode) {
                        String fcstDate = item.path("fcstDate").asText();
                        String category = item.path("category").asText();
                        String fcstValue = item.path("fcstValue").asText();

                        if (todayStr.equals(fcstDate)) {
                            if ("TMP".equals(category)) {
                                try { todayTemps.add((int) Math.round(Double.parseDouble(fcstValue))); } catch (Exception ignored) {}
                            } else if ("TMN".equals(category)) {
                                try { tmn = (int) Math.round(Double.parseDouble(fcstValue)); } catch (Exception ignored) {}
                            } else if ("TMX".equals(category)) {
                                try { tmx = (int) Math.round(Double.parseDouble(fcstValue)); } catch (Exception ignored) {}
                            }
                        }
                    }

                    if (tmn == null && !todayTemps.isEmpty()) {
                        tmn = Collections.min(todayTemps);
                    }
                    if (tmx == null && !todayTemps.isEmpty()) {
                        tmx = Collections.max(todayTemps);
                    }

                    forecast.setTodayMinTemp(tmn != null ? tmn : currentTemp);
                    forecast.setTodayMaxTemp(tmx != null ? tmx : currentTemp);

                    // 3) 향후 6시간 예보 (1시간 단위로 순회하여 데이터 매핑)
                    for (int h = 1; h <= 6; h++) {
                        LocalDateTime targetTime = now.plusHours(h);
                        String targetDateStr = targetTime.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
                        String targetTimeStr = String.format("%02d00", targetTime.getHour());

                        int fTemp = 0;
                        int fPop = 0;
                        int fSky = 1;
                        int fPty = 0;
                        boolean found = false;

                        for (JsonNode item : itemsNode) {
                            String fcstDate = item.path("fcstDate").asText();
                            String fcstTime = item.path("fcstTime").asText();
                            if (targetDateStr.equals(fcstDate) && targetTimeStr.equals(fcstTime)) {
                                found = true;
                                String category = item.path("category").asText();
                                String val = item.path("fcstValue").asText();
                                switch (category) {
                                    case "TMP":
                                        try { fTemp = (int) Math.round(Double.parseDouble(val)); } catch (Exception ignored) {}
                                        break;
                                    case "POP":
                                        try { fPop = (int) Math.round(Double.parseDouble(val)); } catch (Exception ignored) {}
                                        break;
                                    case "SKY":
                                        try { fSky = Integer.parseInt(val); } catch (Exception ignored) {}
                                        break;
                                    case "PTY":
                                        try { fPty = Integer.parseInt(val); } catch (Exception ignored) {}
                                        break;
                                }
                            }
                        }

                        if (found) {
                            String fStatus = mapKmaWeatherStatus(fSky, fPty);
                            WeatherHour wh = new WeatherHour();
                            wh.setTime(String.format("%02d:00", targetTime.getHour()));
                            wh.setTemp(fTemp);
                            wh.setStatus(fStatus);
                            wh.setIcon(getWeatherIcon(fStatus));
                            wh.setPop(fPop);
                            hours.add(wh);
                        }
                    }
                }

                success = true;
                log.info("성공적으로 실시간 날씨 정보를 기상청 단기예보 OpenAPI로부터 조회하였습니다.");
            } catch (Exception e) {
                log.error("기상청 OpenAPI 호출 중 에러 발생: {}", e.getMessage());
            }
        }

        if (!success) {
            log.warn("날씨 API 호출 실패로 날씨 정보를 갱신할 수 없습니다.");
            return null;
        }

        forecast.setForecastHours(hours);

        // 4) DB 캐시 업데이트
        try {
            String jsonStr = objectMapper.writeValueAsString(forecast);
            
            WidgetConfig dataConfig = new WidgetConfig();
            dataConfig.setConfigKey("WEATHER_CACHE_DATA");
            dataConfig.setConfigValue(jsonStr);
            widgetDAO.updateConfig(dataConfig);

            WidgetConfig timeConfig = new WidgetConfig();
            timeConfig.setConfigKey("WEATHER_CACHE_TIME");
            timeConfig.setConfigValue(now.toString());
            widgetDAO.updateConfig(timeConfig);
            
            log.info("성공적으로 날씨 정보를 DB 캐시에 저장했습니다.");
        } catch (Exception e) {
            log.error("날씨 정보 DB 캐시 저장 중 예외 발생: {}", e.getMessage());
        }

        // 인메모리 캐시 업데이트
        cachedWeather = forecast;
        weatherCacheTime = now;
        return forecast;
    }

    private String mapWeatherStatus(int id) {
        if (id >= 200 && id < 600) {
            return "비";
        } else if (id >= 600 && id < 700) {
            return "눈";
        } else if (id >= 700 && id < 800) {
            return "흐림";
        } else if (id == 800) {
            return "맑음";
        } else if (id == 801 || id == 802) {
            return "구름조금";
        } else {
            return "흐림";
        }
    }

    private String getWeatherIcon(String status) {
        switch (status) {
            case "맑음": return "fa-sun text-yellow-500";
            case "구름조금": return "fa-cloud-sun text-blue-400";
            case "흐림": return "fa-cloud text-gray-500";
            case "비": return "fa-cloud-showers-heavy text-indigo-500";
            case "비/눈": return "fa-cloud-meatball text-sky-400";
            case "눈": return "fa-snowflake text-sky-300";
            default: return "fa-sun text-yellow-500";
        }
    }

    // ==========================================
    // 4. 밸런스 게임 서비스
    // ==========================================
    public List<BalanceGame> getAllBalanceGames() {
        List<BalanceGame> games = widgetDAO.selectAllBalanceGames();
        if (games == null || games.isEmpty()) {
            return new ArrayList<>();
        }

        for (BalanceGame game : games) {
            // 투표수 집계
            int countA = widgetDAO.selectVoteCount(game.getId(), "A");
            int countB = widgetDAO.selectVoteCount(game.getId(), "B");
            int total = countA + countB;

            game.setCountA(countA);
            game.setCountB(countB);
            game.setTotalCount(total);

            if (total > 0) {
                // 퍼센트 계산
                double pctA = Math.round(((double) countA / total * 100) * 10) / 10.0;
                double pctB = Math.round(((double) countB / total * 100) * 10) / 10.0;
                game.setPercentA(pctA);
                game.setPercentB(pctB);
            } else {
                game.setPercentA(0.0);
                game.setPercentB(0.0);
            }
            game.setVoted(false); // 로컬 스토리지 기반으로 처리하므로 기본값 false
        }

        return games;
    }

    @Transactional
    public Map<String, Object> voteBalanceGame(Long questionId, String selection, String ipAddress) {
        Map<String, Object> result = new HashMap<>();

        // IP 기반 중복 체크 제거 (로컬 스토리지 기반으로 투표 제어가 이루어지므로 서버 측 중복 체크 생략)
        // 단, DB의 uq_balance_vote (question_id, ip_address) 유니크 제약조건 충돌을 피하기 위해
        // ip_address 컬럼에 고유 UUID 문자열을 저장합니다.
        String uniqueIp = UUID.randomUUID().toString();

        // 투표 등록
        BalanceGameVote vote = new BalanceGameVote();
        vote.setQuestionId(questionId);
        vote.setIpAddress(uniqueIp);
        vote.setSelection(selection);

        try {
            int insertResult = widgetDAO.insertVote(vote);
            if (insertResult > 0) {
                // 최신 득표 결과 다시 계산하여 리턴
                int countA = widgetDAO.selectVoteCount(questionId, "A");
                int countB = widgetDAO.selectVoteCount(questionId, "B");
                int total = countA + countB;

                double pctA = total > 0 ? Math.round(((double) countA / total * 100) * 10) / 10.0 : 0.0;
                double pctB = total > 0 ? Math.round(((double) countB / total * 100) * 10) / 10.0 : 0.0;

                result.put("success", true);
                result.put("countA", countA);
                result.put("countB", countB);
                result.put("totalCount", total);
                result.put("percentA", pctA);
                result.put("percentB", pctB);
            } else {
                result.put("success", false);
                result.put("message", "투표 처리 중 서버 오류가 발생했습니다.");
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.error("투표 등록 중 중복 키 예외 발생 (UUID 충돌): ", e);
            result.put("success", false);
            result.put("message", "투표 처리 중 일시적인 오류가 발생했습니다. 다시 시도해 주세요.");
        }

        return result;
    }

    @Transactional
    public boolean createBalanceGame(BalanceGame game) {
        int result = widgetDAO.insertBalanceGame(game);
        return result > 0;
    }

    // ==========================================
    // 내부 헬퍼 클래스 (Weather, Transit 응답용)
    // ==========================================
    @Data
    public static class WeatherForecast {
        private String station;
        private int currentTemp;
        private String currentStatus;
        private String currentIcon;
        private int currentPop;
        private int currentHumidity;
        private double currentWindSpeed;
        private Integer todayMinTemp;
        private Integer todayMaxTemp;
        private List<WeatherHour> forecastHours;
    }

    @Data
    public static class WeatherHour {
        private String time;
        private int temp;
        private String status;
        private String icon;
        private int pop;
    }


}
