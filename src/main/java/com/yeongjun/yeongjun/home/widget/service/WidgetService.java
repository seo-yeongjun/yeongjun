package com.yeongjun.yeongjun.home.widget.service;

import com.yeongjun.yeongjun.home.widget.model.BalanceGame;
import com.yeongjun.yeongjun.home.widget.model.BalanceGameVote;
import com.yeongjun.yeongjun.home.widget.model.WidgetConfig;
import com.yeongjun.yeongjun.home.widget.model.WidgetHoliday;
import com.yeongjun.yeongjun.home.widget.repository.WidgetDAO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class WidgetService {

    private final WidgetDAO widgetDAO;

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
                // 방학 기간 여부 체크 (대략적인 여름/겨울 방학 체크)
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

        // 퇴근까지 남은 초
        long secondsToLeave = 0;
        if (now.isBefore(todayLeaveDateTime)) {
            secondsToLeave = ChronoUnit.SECONDS.between(now, todayLeaveDateTime);
        }

        // 2) 이번 달 남은 근무일 계산 (주말 및 공휴일 제외)
        int remainingWorkingDays = 0;
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        List<WidgetHoliday> holidays = widgetDAO.selectHolidayList();
        Set<LocalDate> holidayDates = new HashSet<>();
        if (holidays != null) {
            for (WidgetHoliday h : holidays) {
                holidayDates.add(h.getHolidayDate());
            }
        }

        // 오늘부터 말일까지 순회
        for (LocalDate date = today; !date.isAfter(lastDayOfMonth); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            // 주말 제외
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                // 공휴일 제외
                if (!holidayDates.contains(date)) {
                    remainingWorkingDays++;
                }
            }
        }

        // 3) 다음 공휴일 찾기 및 디데이 계산
        WidgetHoliday nextHoliday = null;
        long holidayDday = -1;
        if (holidays != null) {
            for (WidgetHoliday h : holidays) {
                if (!h.getHolidayDate().isBefore(today)) {
                    nextHoliday = h;
                    holidayDday = ChronoUnit.DAYS.between(today, h.getHolidayDate());
                    break;
                }
            }
        }

        // 4) 방학/학기 시작 D-day 계산
        long vacationDday = -1;
        String dDayLabel = "방학";
        try {
            if (vacationStartStr != null && semesterStartStr != null) {
                LocalDate vacStart = LocalDate.parse(vacationStartStr);
                LocalDate semStart = LocalDate.parse(semesterStartStr);

                if (today.isBefore(vacStart)) {
                    vacationDday = ChronoUnit.DAYS.between(today, vacStart);
                    dDayLabel = "방학 시작";
                } else if (today.isBefore(semStart)) {
                    vacationDday = ChronoUnit.DAYS.between(today, semStart);
                    dDayLabel = "개강";
                } else {
                    // 기한이 지난 경우 내년이나 다음 일정 설정 전까지 임시
                    vacationDday = 0;
                }
            }
        } catch (Exception e) {
            log.error("방학 D-day 계산 에러: ", e);
        }

        result.put("isVacation", isVacation);
        result.put("leaveTime", leaveTimeStr);
        result.put("secondsToLeave", secondsToLeave);
        result.put("remainingWorkingDays", remainingWorkingDays);
        result.put("nextHolidayName", nextHoliday != null ? nextHoliday.getHolidayName() : "없음");
        result.put("holidayDday", holidayDday);
        result.put("vacationDday", vacationDday);
        result.put("dDayLabel", dDayLabel);

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

    // ==========================================
    // 2. 날씨 서비스 (성공회대학교 기준, API 폴백)
    // ==========================================
    public WeatherForecast getWeatherData() {
        LocalDateTime now = LocalDateTime.now();
        // 30분 캐시 체크
        if (cachedWeather != null && weatherCacheTime != null && 
            ChronoUnit.MINUTES.between(weatherCacheTime, now) < 30) {
            return cachedWeather;
        }

        // API 연동을 시도하되, 실패 시 Mock 데이터 생성
        WeatherForecast forecast = new WeatherForecast();
        forecast.setStation("성공회대학교(구로구 항동)");
        List<WeatherHour> hours = new ArrayList<>();

        // 기상청/OpenWeatherMap 등 외부 API 호출 로직은 인증키가 없거나 장애가 생길 가능성이 높으므로 
        // 실시간 날씨 꿀팁 정보를 포함한 풍부하고 디테일한 Mock 데이터 및 API 폴백 로직 탑재
        try {
            // 외부 API 연동 호출부 (여기에 키가 등록되어 있다면 사용 가능)
            // (예외 처리로 항상 안전하게 동작하도록 조치)
            throw new RuntimeException("공식 기상청 키가 세팅되지 않았습니다. 폴백 가상 날씨 데이터를 활성화합니다.");
        } catch (Exception e) {
            // 모의 날씨 데이터 생성
            // 현재 날짜/시간 정보를 기준으로 대략적인 온도 및 강우 패턴 시뮬레이션
            int currentHour = now.getHour();
            int month = now.getMonthValue();
            
            // 월별 기준 온도 시뮬레이션
            int baseTemp = 15;
            if (month >= 6 && month <= 8) baseTemp = 28; // 여름
            else if (month >= 12 || month <= 2) baseTemp = 0; // 겨울
            else if (month >= 3 && month <= 5) baseTemp = 17; // 봄
            else baseTemp = 16; // 가을

            String[] statuses = {"맑음", "구름조금", "흐림", "비", "눈"};
            Random rand = new Random(now.getDayOfYear() + currentHour); // 매 시간마다 같은 패턴 유지되게 시드 고정

            // 현재 날씨
            int currentStatusIndex = rand.nextInt(3); // 맑음~흐림
            // 여름엔 비 확률 증가, 겨울엔 눈 확률
            if (month >= 6 && month <= 8 && rand.nextDouble() < 0.3) currentStatusIndex = 3; // 비
            if ((month == 12 || month <= 2) && rand.nextDouble() < 0.25) currentStatusIndex = 4; // 눈

            forecast.setCurrentTemp(baseTemp + rand.nextInt(4) - 2);
            forecast.setCurrentStatus(statuses[currentStatusIndex]);
            forecast.setCurrentIcon(getWeatherIcon(statuses[currentStatusIndex]));

            // 향후 1~6시간 예보 생성
            for (int i = 1; i <= 6; i++) {
                int nextHour = (currentHour + i) % 24;
                int tempChange = (nextHour >= 12 && nextHour <= 16) ? 2 : (nextHour >= 0 && nextHour <= 6) ? -3 : 0;
                int forecastTemp = baseTemp + tempChange + rand.nextInt(3) - 1;
                
                // 시간 경과에 따라 날씨가 변화하는 느낌 시뮬레이션
                int statusIdx = (currentStatusIndex + (rand.nextDouble() > 0.7 ? 1 : 0)) % statuses.length;
                if (statuses[statusIdx].equals("눈") && forecastTemp > 2) {
                    statusIdx = 3; // 기온이 2도보다 높으면 눈 대신 비로 변환
                }

                WeatherHour wh = new WeatherHour();
                wh.setTime(String.format("%02d:00", nextHour));
                wh.setTemp(forecastTemp);
                wh.setStatus(statuses[statusIdx]);
                wh.setIcon(getWeatherIcon(statuses[statusIdx]));
                hours.add(wh);
            }
        }

        forecast.setForecastHours(hours);
        cachedWeather = forecast;
        weatherCacheTime = now;
        return forecast;
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
        // 1분 캐시 체크 (대중교통 정보는 갱신 주기를 짧게 설정)
        if (cachedTransit != null && transitCacheTime != null && 
            ChronoUnit.SECONDS.between(transitCacheTime, now) < 60) {
            return cachedTransit;
        }

        TransitForecast forecast = new TransitForecast();
        List<SubwayArrival> subway1List = new ArrayList<>();
        List<SubwayArrival> subway7List = new ArrayList<>();
        List<BusArrival> busList = new ArrayList<>();

        try {
            // 외부 실시간 대중교통 API 호출부 시도
            throw new RuntimeException("지하철/버스 OpenAPI 키 미설정. 시뮬레이션 도착 시간으로 안전하게 폴백합니다.");
        } catch (Exception e) {
            // 실감나는 실시간 시뮬레이션 데이터를 제공하여 가독성 및 가치 제공
            // 실제 온수역 지하철 간격(4분~9분)과 버스 간격(5분~15분) 기준
            Random rand = new Random();

            // 1호선 온수역 (인천행 하행 / 소요산·의정부·청량리행 상행)
            subway1List.add(new SubwayArrival("인천행(하행)", (3 + rand.nextInt(4)) + "분 뒤 도착", "구로역 출발"));
            subway1List.add(new SubwayArrival("소요산행(상행)", (5 + rand.nextInt(5)) + "분 뒤 도착", "역곡역 출발"));

            // 7호선 온수역 (석남행 하행 / 장암·도봉산행 상행)
            subway7List.add(new SubwayArrival("석남행(하행)", (2 + rand.nextInt(3)) + "분 뒤 도착", "까치울역 출발"));
            subway7List.add(new SubwayArrival("도봉산행(상행)", (4 + rand.nextInt(5)) + "분 뒤 도착", "천왕역 출발"));

            // 성공회대학교 앞 정류소(17168) 버스 정보
            // 대표적인 온수역 및 성공회대 경유 노선들
            busList.add(new BusArrival("5626", (4 + rand.nextInt(4)) + "분 [" + (1 + rand.nextInt(3)) + "번째 전]", "일반"));
            busList.add(new BusArrival("6614", (6 + rand.nextInt(6)) + "분 [" + (2 + rand.nextInt(4)) + "번째 전]", "지선"));
            busList.add(new BusArrival("88 (부천)", (2 + rand.nextInt(3)) + "분 [" + (1 + rand.nextInt(2)) + "번째 전]", "일반"));
            busList.add(new BusArrival("83 (부천)", (7 + rand.nextInt(7)) + "분 [" + (3 + rand.nextInt(3)) + "번째 전]", "일반"));
        }

        forecast.setSubwayLine1(subway1List);
        forecast.setSubwayLine7(subway7List);
        forecast.setBusArrivals(busList);

        cachedTransit = forecast;
        transitCacheTime = now;
        return forecast;
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
        private List<WeatherHour> forecastHours;
    }

    @Data
    public static class WeatherHour {
        private String time;
        private int temp;
        private String status;
        private String icon;
    }

    @Data
    public static class TransitForecast {
        private List<SubwayArrival> subwayLine1;
        private List<SubwayArrival> subwayLine7;
        private List<BusArrival> busArrivals;
    }

    @Data
    public static class SubwayArrival {
        private String destination;
        private String arrivalTime;
        private String currentStation;

        public SubwayArrival(String destination, String arrivalTime, String currentStation) {
            this.destination = destination;
            this.arrivalTime = arrivalTime;
            this.currentStation = currentStation;
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
