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

    @Value("${seoul.subway-key:}")
    private String subwayApiKey;

    @Value("${seoul.bus-key:}")
    private String busApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 캐시 저장소
    private WeatherForecast cachedWeather = null;
    private LocalDateTime weatherCacheTime = null;

    private TransitForecast cachedTransit = null;
    private LocalDateTime transitCacheTime = null;

    public WidgetService(WidgetDAO widgetDAO) {
        this.widgetDAO = widgetDAO;
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
    public WeatherForecast getWeatherData() {
        LocalDateTime now = LocalDateTime.now();
        
        // 1) 15분 인메모리 캐시 체크 (트래픽 효율 극대화)
        if (cachedWeather != null && weatherCacheTime != null && 
            ChronoUnit.MINUTES.between(weatherCacheTime, now) < 15) {
            return cachedWeather;
        }

        // 2) DB 캐시 체크
        String dbTimeStr = widgetDAO.selectConfig("WEATHER_CACHE_TIME");
        String dbDataStr = widgetDAO.selectConfig("WEATHER_CACHE_DATA");

        if (dbTimeStr != null && !dbTimeStr.trim().isEmpty() &&
            dbDataStr != null && !dbDataStr.trim().isEmpty()) {
            try {
                LocalDateTime dbTime = LocalDateTime.parse(dbTimeStr);
                if (ChronoUnit.MINUTES.between(dbTime, now) < 15) {
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
                // 1) Current Weather
                String currentWeatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=37.4878&lon=126.8258&appid=" + weatherApiKey.trim() + "&units=metric&lang=kr";
                String currentRes = restTemplate.getForObject(currentWeatherUrl, String.class);
                JsonNode currentRoot = objectMapper.readTree(currentRes);

                double tempDouble = currentRoot.path("main").path("temp").asDouble();
                int currentTemp = (int) Math.round(tempDouble);
                int weatherId = currentRoot.path("weather").path(0).path("id").asInt();
                String status = mapWeatherStatus(weatherId);

                forecast.setCurrentTemp(currentTemp);
                forecast.setCurrentStatus(status);
                forecast.setCurrentIcon(getWeatherIcon(status));

                // 2) Forecast Weather (5-day / 3-hour forecast)
                String forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=37.4878&lon=126.8258&appid=" + weatherApiKey.trim() + "&units=metric&lang=kr";
                String forecastRes = restTemplate.getForObject(forecastUrl, String.class);
                JsonNode forecastRoot = objectMapper.readTree(forecastRes);
                JsonNode listNode = forecastRoot.path("list");

                if (listNode.isArray()) {
                    if (listNode.size() > 0) {
                        double firstPop = listNode.get(0).path("pop").asDouble();
                        forecast.setCurrentPop((int) Math.round(firstPop * 100));
                    } else {
                        forecast.setCurrentPop(0);
                    }

                    // Raw forecast points list for interpolation
                    List<ForecastPoint> points = new ArrayList<>();
                    
                    // Add Point 0 (Current Weather)
                    ForecastPoint p0 = new ForecastPoint();
                    p0.hoursFromNow = 0.0;
                    p0.temp = currentTemp;
                    p0.pop = forecast.getCurrentPop();
                    p0.status = status;
                    p0.icon = forecast.getCurrentIcon();
                    points.add(p0);

                    long nowEpoch = now.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();

                    for (JsonNode item : listNode) {
                        long dt = item.path("dt").asLong();
                        double diffHours = (double) (dt - nowEpoch) / 3600.0;
                        if (diffHours < 0) continue; // Skip past points

                        double fTempDouble = item.path("main").path("temp").asDouble();
                        int fTemp = (int) Math.round(fTempDouble);
                        int fWeatherId = item.path("weather").path(0).path("id").asInt();
                        String fStatus = mapWeatherStatus(fWeatherId);
                        double fPopDouble = item.path("pop").asDouble();
                        int fPop = (int) Math.round(fPopDouble * 100);

                        ForecastPoint fp = new ForecastPoint();
                        fp.hoursFromNow = diffHours;
                        fp.temp = fTemp;
                        fp.pop = fPop;
                        fp.status = fStatus;
                        fp.icon = getWeatherIcon(fStatus);
                        points.add(fp);

                        // Fetch enough forecast points (up to 12 hours out) to interpolate 6 hourly slots
                        if (diffHours >= 12.0) {
                            break;
                        }
                    }

                    // Perform linear interpolation for h = 1..6 hours from now
                    for (int h = 1; h <= 6; h++) {
                        double targetHour = (double) h;
                        ForecastPoint left = null;
                        ForecastPoint right = null;

                        for (ForecastPoint p : points) {
                            if (p.hoursFromNow <= targetHour) {
                                if (left == null || p.hoursFromNow > left.hoursFromNow) {
                                    left = p;
                                }
                            }
                            if (p.hoursFromNow >= targetHour) {
                                if (right == null || p.hoursFromNow < right.hoursFromNow) {
                                    right = p;
                                }
                            }
                        }

                        if (left == null) left = points.get(0);
                        if (right == null) right = points.get(points.size() - 1);

                        int finalTemp;
                        int finalPop;
                        String finalStatus;
                        String finalIcon;

                        if (left == right || Math.abs(right.hoursFromNow - left.hoursFromNow) < 0.001) {
                            finalTemp = left.temp;
                            finalPop = left.pop;
                            finalStatus = left.status;
                            finalIcon = left.icon;
                        } else {
                            double fraction = (targetHour - left.hoursFromNow) / (right.hoursFromNow - left.hoursFromNow);
                            finalTemp = (int) Math.round(left.temp + (right.temp - left.temp) * fraction);
                            finalPop = (int) Math.round(left.pop + (right.pop - left.pop) * fraction);
                            if (fraction < 0.5) {
                                finalStatus = left.status;
                                finalIcon = left.icon;
                            } else {
                                finalStatus = right.status;
                                finalIcon = right.icon;
                            }
                        }

                        LocalDateTime targetTime = now.plusHours(h);
                        WeatherHour wh = new WeatherHour();
                        wh.setTime(String.format("%02d:00", targetTime.getHour()));
                        wh.setTemp(finalTemp);
                        wh.setStatus(finalStatus);
                        wh.setIcon(finalIcon);
                        wh.setPop(finalPop);
                        hours.add(wh);
                    }
                }

                success = true;
                log.info("성공적으로 실시간 날씨 정보를 OpenWeatherMap API로부터 조회 및 1시간 단위 보간 처리했습니다.");
            } catch (Exception e) {
                log.error("OpenWeatherMap API 호출 중 에러 발생: {}", e.getMessage());
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
            case "눈": return "fa-snowflake text-sky-300";
            default: return "fa-sun text-yellow-500";
        }
    }

    // ==========================================
    // 3. 교통 서비스 (온수역 지하철, 버스, API 폴백)
    // ==========================================
    public TransitForecast getTransitData() {
        LocalDateTime now = LocalDateTime.now();
        // 30초 캐시 체크 (대중교통 정보는 실시간성이 중요하므로 캐시 주기를 30초로 지정)
        if (cachedTransit != null && transitCacheTime != null && 
            ChronoUnit.SECONDS.between(transitCacheTime, now) < 30) {
            return cachedTransit;
        }

        TransitForecast forecast = new TransitForecast();
        
        // 1) 1호선 온수역
        forecast.setSubwayLine1(fetchSubwayArrivals("1001"));
        
        // 2) 7호선 온수역
        forecast.setSubwayLine7(fetchSubwayArrivals("1007"));
        
        // 3) 버스 17999 (성공회대입구)
        forecast.setBus17999(fetchBusArrivals("17999"));
        
        // 4) 버스 17117 (온수역)
        forecast.setBus17117(fetchBusArrivals("17117"));
        
        // 5) 버스 17682 (온수역종점)
        forecast.setBus17682(fetchBusArrivals("17682"));

        cachedTransit = forecast;
        transitCacheTime = now;
        return forecast;
    }

    private String formatSubwayArrivalTime(String arvlMsg2, String barvlDtStr) {
        if (arvlMsg2 == null || arvlMsg2.isEmpty()) {
            return "-";
        }
        
        // 1. 온수 도착, 온수 진입 등 바로 도착 예정인 경우
        if (arvlMsg2.contains("온수 도착") || arvlMsg2.contains("온수 진입") || arvlMsg2.contains("당역 도착") || arvlMsg2.contains("당역 진입") || arvlMsg2.contains("당역 도착예정")) {
            return "도착 임박";
        }
        
        // 2. barvlDt가 유효한 초 단위 값인 경우 (0보다 크고 30분 미만인 경우만 신뢰)
        try {
            if (barvlDtStr != null && !barvlDtStr.isEmpty()) {
                int seconds = Integer.parseInt(barvlDtStr);
                if (seconds > 0 && seconds < 1800) {
                    int mins = seconds / 60;
                    if (mins == 0) return "도착 임박";
                    return mins + "분 뒤";
                }
            }
        } catch (NumberFormatException ignored) {}

        // 3. 메시지 파싱
        // "11분 후 (부천시청)" -> "11분 뒤"
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)분").matcher(arvlMsg2);
        if (m.find()) {
            return m.group(1) + "분 뒤";
        }
        
        if (arvlMsg2.contains("전역 도착") || arvlMsg2.contains("전역 진입")) {
            return "전역 (도착 임박)";
        }
        
        // "[5]번째 전역" -> "5역 전" (또는 대략 10분 뒤)
        m = java.util.regex.Pattern.compile("\\[(\\d+)\\]번째").matcher(arvlMsg2);
        if (m.find()) {
            int stations = Integer.parseInt(m.group(1));
            return (stations * 2) + "분 뒤"; // 1역당 약 2분 계산
        }
        
        return arvlMsg2; // 파싱 실패 시 원본 메시지 반환
    }

    private List<SubwayArrival> fetchSubwayArrivals(String lineId) {
        if (subwayApiKey == null || subwayApiKey.trim().isEmpty() || "sample".equals(subwayApiKey)) {
            return null;
        }
        try {
            String encodedStation = java.net.URLEncoder.encode("온수", "UTF-8");
            String url = "http://swopenapi.seoul.go.kr/api/subway/" + subwayApiKey + "/json/realtimeStationArrival/0/20/" + encodedStation;
            String responseStr = restTemplate.getForObject(java.net.URI.create(url), String.class);
            JsonNode root = objectMapper.readTree(responseStr);
            JsonNode listNode = root.get("realtimeArrivalList");
            if (listNode != null && listNode.isArray()) {
                List<SubwayArrival> result = new ArrayList<>();
                List<SubwayArrival> downTrains = new ArrayList<>();
                List<SubwayArrival> upTrains = new ArrayList<>();
                Set<String> processedTrainNos = new HashSet<>();

                for (JsonNode node : listNode) {
                    String subId = node.path("subwayId").asText();
                    if (!lineId.equals(subId)) {
                        continue;
                    }
                    String trainNo = node.path("btrainNo").asText();
                    if (trainNo != null && !trainNo.isEmpty()) {
                        if (processedTrainNos.contains(trainNo)) {
                            continue;
                        }
                        processedTrainNos.add(trainNo);
                    }

                    String updnLine = node.path("updnLine").asText();
                    String trainLineNm = node.path("trainLineNm").asText();
                    String arvlMsg2 = node.path("arvlMsg2").asText();
                    String arvlMsg3 = node.path("arvlMsg3").asText();
                    String barvlDt = node.path("barvlDt").asText();
                    String btrainSttus = node.path("btrainSttus").asText();

                    // 온수역에는 급행/특급 열차가 정차하지 않으므로 필터링 (용산행 급행 등 통과 열차 제거)
                    if ("급행".equals(btrainSttus) || "특급".equals(btrainSttus) || 
                        trainLineNm.contains("급행") || trainLineNm.contains("특급")) {
                        continue;
                    }

                    boolean isDown = "1".equals(updnLine) || "하행".equals(updnLine) || "외선".equals(updnLine) || trainLineNm.contains("인천") || trainLineNm.contains("석남");
                    boolean isUp = "0".equals(updnLine) || "상행".equals(updnLine) || "내선".equals(updnLine) || trainLineNm.contains("소요산") || trainLineNm.contains("도봉산") || trainLineNm.contains("장암");

                    String dest = trainLineNm;
                    if (dest.contains("-")) {
                        dest = dest.split("-")[0].trim();
                    }

                    // 온수역 종착(온수행) 열차는 탑승할 수 없으므로 필터링
                    if (dest.contains("온수")) {
                        continue;
                    }

                    String formattedArrival = formatSubwayArrivalTime(arvlMsg2, barvlDt);
                    String direction = isDown ? "하행" : "상행";

                    SubwayArrival arrival = new SubwayArrival(dest, formattedArrival, arvlMsg3, direction);

                    if (isDown && downTrains.size() < 2) {
                        downTrains.add(arrival);
                    } else if (isUp && upTrains.size() < 2) {
                        upTrains.add(arrival);
                    }
                }

                result.addAll(downTrains);
                result.addAll(upTrains);
                return result;
            }
        } catch (Exception e) {
            log.error("지하철 API 호출 실패 (온수역, 호선: {}): {}", lineId, e.getMessage());
        }
        return null;
    }



    private List<BusArrival> fetchBusArrivals(String arsId) {
        if (busApiKey == null || busApiKey.trim().isEmpty() || "sample".equals(busApiKey)) {
            return null;
        }
        try {
            String url = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?ServiceKey=" + busApiKey + "&arsId=" + arsId + "&resultType=json";
            String responseStr = restTemplate.getForObject(java.net.URI.create(url), String.class);
            JsonNode root = objectMapper.readTree(responseStr);
            JsonNode items = root.path("ServiceResult").path("msgBody").path("itemList");
            List<BusArrival> arrivals = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    String busNo = item.path("rtNm").asText();
                    String arrmsg1 = item.path("arrmsg1").asText();
                    String routeType = item.path("routeType").asText();
                    
                    if ("운행종료".equals(arrmsg1) || "정류소 미정차".equals(arrmsg1)) {
                        continue;
                    }
                    
                    arrivals.add(new BusArrival(busNo, arrmsg1, mapRouteType(routeType)));
                }
            }
            return arrivals;
        } catch (Exception e) {
            log.error("버스 API 호출 실패 (arsId: {}): {}", arsId, e.getMessage());
            return null;
        }
    }

    private String mapRouteType(String routeType) {
        if ("3".equals(routeType)) return "간선";
        if ("4".equals(routeType)) return "지선";
        if ("5".equals(routeType)) return "순환";
        if ("6".equals(routeType)) return "광역";
        return "일반";
    }

    // ==========================================
    // 4. 밸런스 게임 서비스
    // ==========================================
    public BalanceGame getActiveBalanceGame(String ipAddress) {
        BalanceGame activeGame = widgetDAO.selectActiveBalanceGame();
        if (activeGame == null) {
            return null;
        }

        // 투표수 집계
        int countA = widgetDAO.selectVoteCount(activeGame.getId(), "A");
        int countB = widgetDAO.selectVoteCount(activeGame.getId(), "B");
        int total = countA + countB;

        activeGame.setCountA(countA);
        activeGame.setCountB(countB);
        activeGame.setTotalCount(total);

        if (total > 0) {
            // 퍼센트 계산
            double pctA = Math.round(((double) countA / total * 100) * 10) / 10.0;
            double pctB = Math.round(((double) countB / total * 100) * 10) / 10.0;
            activeGame.setPercentA(pctA);
            activeGame.setPercentB(pctB);
        } else {
            activeGame.setPercentA(0.0);
            activeGame.setPercentB(0.0);
        }

        // 해당 IP의 투표 참여 여부 검증
        int votedCount = widgetDAO.checkIpVoted(activeGame.getId(), ipAddress);
        activeGame.setVoted(votedCount > 0);

        return activeGame;
    }

    @Transactional
    public Map<String, Object> voteBalanceGame(Long questionId, String selection, String ipAddress) {
        Map<String, Object> result = new HashMap<>();

        // 중복 투표 체크
        int count = widgetDAO.checkIpVoted(questionId, ipAddress);
        if (count > 0) {
            result.put("success", false);
            result.put("message", "이미 이 밸런스 게임에 투표하셨습니다.");
            return result;
        }

        // 투표 등록
        BalanceGameVote vote = new BalanceGameVote();
        vote.setQuestionId(questionId);
        vote.setIpAddress(ipAddress);
        vote.setSelection(selection);

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

    private static class ForecastPoint {
        double hoursFromNow;
        int temp;
        int pop;
        String status;
        String icon;
    }

    @Data
    public static class TransitForecast {
        private List<SubwayArrival> subwayLine1;
        private List<SubwayArrival> subwayLine7;
        private List<BusArrival> bus17999;
        private List<BusArrival> bus17117;
        private List<BusArrival> bus17682;
    }

    @Data
    public static class SubwayArrival {
        private String destination;
        private String arrivalTime;
        private String currentStation;
        private String direction; // 상행 / 하행

        public SubwayArrival(String destination, String arrivalTime, String currentStation, String direction) {
            this.destination = destination;
            this.arrivalTime = arrivalTime;
            this.currentStation = currentStation;
            this.direction = direction;
        }
    }

    @Data
    public static class BusArrival {
        private String busNo;
        private String arrivalTime;
        private String type; // 일반, 지선 등

        public BusArrival(String busNo, String arrivalTime, String type) {
            this.busNo = busNo;
            this.arrivalTime = arrivalTime;
            this.type = type;
        }
    }
}
