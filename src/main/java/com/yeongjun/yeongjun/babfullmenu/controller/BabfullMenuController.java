package com.yeongjun.yeongjun.babfullmenu.controller;

import com.yeongjun.yeongjun.babfullmenu.entity.MenuDTO;
import com.yeongjun.yeongjun.babfullmenu.model.BabfullMenu;
import com.yeongjun.yeongjun.babfullmenu.service.DocumentAIService;
import com.yeongjun.yeongjun.transactions.service.BabfullMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;


@Controller
@RequestMapping("/babfullmenu")
@Slf4j
public class BabfullMenuController {
    private final BabfullMenuService babfullMenuService;
    private final DocumentAIService documentAIService;

    public BabfullMenuController(BabfullMenuService babfullMenuService, DocumentAIService documentAIService) {
        this.babfullMenuService = babfullMenuService;
        this.documentAIService = documentAIService;
    }

    @GetMapping({"", "/"})
    public String toolsHome(Model model) {
        model.addAttribute("babfullmenu", babfullMenuService.getRelevantMenu());
        return "babfullmenu/home";
    }

    @PostMapping("/upload")
    public String uploadBabfullMenuImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("provider") String provider,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // 오늘 메뉴가 이미 존재하는지 확인
            if (!babfullMenuService.getRelevantMenu().isEmpty()) {
                redirectAttributes.addFlashAttribute("info", "오늘의 메뉴가 이미 등록되었습니다.");
                return "redirect:/babfullmenu";
            }

            // 파일과 프로바이더가 비어있는지 확인
            if (file.isEmpty() || provider.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "파일과 이름을 모두 입력해주세요.");
                return "redirect:/babfullmenu";
            }

            // 로그 기록
            String originalFilename = file.getOriginalFilename();
            log.info("오리진네임: {}", originalFilename);
            log.info("프로바이더: {}", provider);

            // Document AI를 사용하여 메뉴 파싱
            MenuDTO menuDTO = documentAIService.processDocumentToMenuDTO(file);

            // 날짜 문자열 파싱
            // 날짜 문자열 파싱
            String startDtString = menuDTO.getStart_dt().trim();       // 예: "12월 16일"
            startDtString = startDtString.replaceAll("\\s+", "");      // 모든 공백 제거
            startDtString = startDtString.replaceAll("[^\\d월일]", ""); // 숫자, '월', '일' 이외의 문자 제거

            String endDtString = menuDTO.getEnd_dt().trim();           // 예: "12월 20일(금)"
            endDtString = endDtString.replaceAll("\\(.*\\)", "").trim(); // 결과: "12월 20일"
            endDtString = endDtString.replaceAll("\\s+", "");         // 모든 공백 제거
            endDtString = endDtString.replaceAll("[^\\d월일]", "");   // 숫자, '월', '일' 이외의 문자 제거

            // 기본 연도 설정 (현재 연도 또는 특정 연도)
            int year = LocalDate.now().getYear(); // 또는 원하는 연도 입력

            // DateTimeFormatter 설정
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("M월d일")
                    .parseDefaulting(ChronoField.YEAR, year)
                    .toFormatter(Locale.KOREAN);

            // LocalDate로 변환
            LocalDate start_dt = LocalDate.parse(startDtString, formatter);
            LocalDate end_dt = LocalDate.parse(endDtString, formatter);

            // 메뉴 리스트 추출
            List<String> lunchDayList1 = menuDTO.getLunch_day1();
            List<String> lunchDayList2 = menuDTO.getLunch_day2();
            List<String> lunchDayList3 = menuDTO.getLunch_day3();
            List<String> lunchDayList4 = menuDTO.getLunch_day4();
            List<String> lunchDayList5 = menuDTO.getLunch_day5();
            List<String> morningDayList1 = menuDTO.getMorning_day1();
            List<String> morningDayList2 = menuDTO.getMorning_day2();
            List<String> morningDayList3 = menuDTO.getMorning_day3();
            List<String> morningDayList4 = menuDTO.getMorning_day4();
            List<String> morningDayList5 = menuDTO.getMorning_day5();

            // BabfullMenu 리스트 생성
            List<BabfullMenu> babfullMenus = new BabfullMenu().convertToBabfullMenuList(
                    start_dt,
                    end_dt,
                    provider,
                    lunchDayList1,
                    lunchDayList2,
                    lunchDayList3,
                    lunchDayList4,
                    lunchDayList5,
                    morningDayList1,
                    morningDayList2,
                    morningDayList3,
                    morningDayList4,
                    morningDayList5
            );

            // BabfullMenu 리스트를 데이터베이스에 삽입
            babfullMenuService.insertBabfullMenus(babfullMenus);

            // 성공 메시지 설정
            redirectAttributes.addFlashAttribute("success", "메뉴가 성공적으로 업로드되었습니다.");
            return "redirect:/babfullmenu";
        } catch (Exception e) {
            log.error("메뉴 업로드 중 오류 발생: ", e);
            redirectAttributes.addFlashAttribute("error", "이미지 파싱 중 오류가 발생했습니다. 영준이에게 이미지와 함께 제보해주세요.");
            return "redirect:/babfullmenu";
        }
    }
}
