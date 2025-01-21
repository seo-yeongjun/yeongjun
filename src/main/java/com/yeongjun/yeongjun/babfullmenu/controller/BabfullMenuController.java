package com.yeongjun.yeongjun.babfullmenu.controller;

import com.yeongjun.yeongjun.babfullmenu.entity.MenuDTO;
import com.yeongjun.yeongjun.babfullmenu.model.BabfullMenu;
import com.yeongjun.yeongjun.babfullmenu.service.DocumentAIService;
import com.yeongjun.yeongjun.transactions.service.BabfullMenuService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/babfullmenu")
@Slf4j
public class BabfullMenuController {
    private final BabfullMenuService babfullMenuService;
    private final DocumentAIService documentAIService;
    // ip별 업로드 횟수 제한을 위한 Map
    private final Map<String, Integer> uploadCountMap = new HashMap<>();

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
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            String ip = request.getRemoteAddr();
            if (uploadCountMap.containsKey(ip)) {
                int count = uploadCountMap.get(ip);
                if (count >= 3) {
                    redirectAttributes.addFlashAttribute("error", "error 999");
                    return "redirect:/babfullmenu";
                }
            }

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

            if (!menuDTO.getIs_menu().contains("주")) {
                if (uploadCountMap.containsKey(ip)) {
                    uploadCountMap.compute(ip, (k, count) -> count + 1);
                } else {
                    uploadCountMap.put(ip, 1);
                }

                redirectAttributes.addFlashAttribute("error", "이미지 파싱 중 오류가 발생했습니다. 영준이에게 이미지와 함께 제보해주세요.");
                return "redirect:/babfullmenu";
            }

            // 1) 먼저 start_dt와 end_dt에 대해 null / 빈 문자열 체크
            String rawStartDt = menuDTO.getStart_dt();
            if (rawStartDt == null) rawStartDt = "";
            rawStartDt = rawStartDt.trim();

            String rawEndDt = menuDTO.getEnd_dt();
            if (rawEndDt == null) rawEndDt = "";
            rawEndDt = rawEndDt.trim();

            // 2) 위에서 받은 rawStartDt / rawEndDt가 비어있지 않을 때만 정규식 처리
            String startDtString = "";
            String endDtString = "";

            // start_dt가 빈 문자열이 아닐 경우
            if (!rawStartDt.isEmpty()) {
                startDtString = rawStartDt
                        .replaceAll("\\(.*\\)", "")    // 괄호 안 내용 제거
                        .replaceAll("\\s+", "")       // 모든 공백 제거
                        .replaceAll("[^\\d월일]", ""); // 숫자, '월', '일' 제외 모두 제거
            }

            // end_dt가 빈 문자열이 아닐 경우
            if (!rawEndDt.isEmpty()) {
                endDtString = rawEndDt
                        .replaceAll("\\(.*\\)", "")
                        .replaceAll("\\s+", "")
                        .replaceAll("[^\\d월일]", "");
            }

            // 3) 문자열 파싱해서 LocalDate 생성
            LocalDate startDate = null;
            LocalDate endDate = null;

            if (!startDtString.isEmpty()) {
                startDate = babfullMenuService.parseMonthDay(startDtString);
            }
            if (!endDtString.isEmpty()) {
                endDate = babfullMenuService.parseMonthDay(endDtString);
            }

            // 4) startDate 혹은 endDate 중 하나가 비었을 경우, 다른 날짜 기준 ±5일 설정
            //    (비즈니스 로직에 따라 바꿀 수 있음)
            if (startDate == null && endDate != null) {
                startDate = endDate.minusDays(5);
            } else if (endDate == null && startDate != null) {
                endDate = startDate.plusDays(5);
            }

            // 둘 다 비어있으면 디폴트값 설정 (예: 오늘 날짜 ~ 오늘+5일)
            if (startDate == null && endDate == null) {
                startDate = LocalDate.now();
                endDate = LocalDate.now().plusDays(5);
            }
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
                    startDate,
                    endDate,
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

            uploadCountMap.clear();

            return "redirect:/babfullmenu";
        } catch (Exception e) {
            String ip = request.getRemoteAddr();
            if (uploadCountMap.containsKey(ip)) {
                uploadCountMap.compute(ip, (k, count) -> count + 1);
            } else {
                uploadCountMap.put(ip, 1);
            }
            log.error("메뉴 업로드 중 오류 발생: ", e);
            redirectAttributes.addFlashAttribute("error", "이미지 파싱 중 오류가 발생했습니다. 영준이에게 이미지와 함께 제보해주세요.");
            return "redirect:/babfullmenu";
        }
    }
}
