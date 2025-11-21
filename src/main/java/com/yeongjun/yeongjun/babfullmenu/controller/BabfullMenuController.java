package com.yeongjun.yeongjun.babfullmenu.controller;

import com.yeongjun.yeongjun.babfullmenu.entity.MenuDTO;
import com.yeongjun.yeongjun.babfullmenu.model.BabfullMenu;
import com.yeongjun.yeongjun.babfullmenu.model.MenuLikeDislike;
import com.yeongjun.yeongjun.babfullmenu.service.DocumentAIService;
import com.yeongjun.yeongjun.babfullmenu.service.MenuLikeDislikeService;
import com.yeongjun.yeongjun.transactions.service.BabfullMenuService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    private final MenuLikeDislikeService menuLikeDislikeService;
    // ip별 업로드 횟수 제한을 위한 Map
    private final Map<String, Integer> uploadCountMap = new HashMap<>();

    public BabfullMenuController(BabfullMenuService babfullMenuService, DocumentAIService documentAIService, MenuLikeDislikeService menuLikeDislikeService) {
        this.babfullMenuService = babfullMenuService;
        this.documentAIService = documentAIService;
        this.menuLikeDislikeService = menuLikeDislikeService;
    }

    @GetMapping({"", "/"})
    public String toolsHome(Model model, HttpServletRequest request, HttpServletResponse response) {
        List<BabfullMenu> menus = babfullMenuService.getRelevantMenu();
        model.addAttribute("babfullmenu", menus);

        Map<String, MenuLikeDislikeService.MenuIdentifier> menuIdentifierMap = new HashMap<>();
        for (BabfullMenu menu : menus) {
            if (menu.getMenu_dt() == null) {
                continue;
            }
            if (menu.getMorning_menu1() != null && !menu.getMorning_menu1().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getMorning_menu1()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getMorning_menu1()));
            if (menu.getMorning_menu2() != null && !menu.getMorning_menu2().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getMorning_menu2()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getMorning_menu2()));
            if (menu.getMorning_menu3() != null && !menu.getMorning_menu3().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getMorning_menu3()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getMorning_menu3()));
            if (menu.getMorning_menu4() != null && !menu.getMorning_menu4().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getMorning_menu4()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getMorning_menu4()));
            if (menu.getMorning_menu5() != null && !menu.getMorning_menu5().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getMorning_menu5()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getMorning_menu5()));
            if (menu.getMorning_menu6() != null && !menu.getMorning_menu6().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getMorning_menu6()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getMorning_menu6()));
            if (menu.getMorning_menu7() != null && !menu.getMorning_menu7().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getMorning_menu7()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getMorning_menu7()));
            if (menu.getMorning_menu8() != null && !menu.getMorning_menu8().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getMorning_menu8()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getMorning_menu8()));
            if (menu.getMorning_menu9() != null && !menu.getMorning_menu9().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getMorning_menu9()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getMorning_menu9()));
            if (menu.getLunch_menu1() != null && !menu.getLunch_menu1().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getLunch_menu1()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getLunch_menu1()));
            if (menu.getLunch_menu2() != null && !menu.getLunch_menu2().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getLunch_menu2()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getLunch_menu2()));
            if (menu.getLunch_menu3() != null && !menu.getLunch_menu3().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getLunch_menu3()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getLunch_menu3()));
            if (menu.getLunch_menu4() != null && !menu.getLunch_menu4().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getLunch_menu4()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getLunch_menu4()));
            if (menu.getLunch_menu5() != null && !menu.getLunch_menu5().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getLunch_menu5()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getLunch_menu5()));
            if (menu.getLunch_menu6() != null && !menu.getLunch_menu6().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getLunch_menu6()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getLunch_menu6()));
            if (menu.getLunch_menu7() != null && !menu.getLunch_menu7().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getLunch_menu7()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getLunch_menu7()));
            if (menu.getLunch_menu8() != null && !menu.getLunch_menu8().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getLunch_menu8()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getLunch_menu8()));
            if (menu.getLunch_menu9() != null && !menu.getLunch_menu9().isEmpty()) menuIdentifierMap.put(buildMenuKey(menu.getMenu_dt(), menu.getLunch_menu9()), new MenuLikeDislikeService.MenuIdentifier(menu.getMenu_dt(), menu.getLunch_menu9()));
        }

        String sessionId = getOrCreateSessionId(request, response);

        Map<String, com.yeongjun.yeongjun.babfullmenu.model.MenuLikeDislike> likeDislikeMap =
            menuLikeDislikeService.getLikeDislikeMap(menuIdentifierMap);
        Map<String, String> currentVoteMap =
            menuLikeDislikeService.getCurrentVoteMap(menuIdentifierMap, sessionId);

        model.addAttribute("likeDislikeMap", likeDislikeMap);
        model.addAttribute("currentVoteMap", currentVoteMap);

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

    /**
     * 세션 ID를 가져오거나 생성합니다 (쿠키 기반)
     */
    private String getOrCreateSessionId(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("MENU_VOTE_SESSION_ID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        // 세션 ID가 없으면 생성
        String sessionId = java.util.UUID.randomUUID().toString();
        Cookie cookie = new Cookie("MENU_VOTE_SESSION_ID", sessionId);
        cookie.setMaxAge(24 * 60 * 60); // 24시간
        cookie.setPath("/");
        response.addCookie(cookie);
        return sessionId;
    }

    private String buildMenuKey(LocalDate menuDate, String menuName) {
        return menuDate.toString() + "|" + menuName;
    }

    /**
     * 좋아요/싫어요 투표 API
     */
    @PostMapping("/vote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vote(
            @RequestParam("menuName") String menuName,
            @RequestParam("menuDate") String menuDate,
            @RequestParam("voteType") String voteType,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String sessionId = getOrCreateSessionId(request, response);
            Map<String, Object> result = menuLikeDislikeService.vote(
                    menuName,
                    LocalDate.parse(menuDate),
                    voteType,
                    sessionId
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("vote 처리 오류 발생: ", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "투표 처리 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResult);
        }
    }

@GetMapping("/likeDislike")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLikeDislike(
            @RequestParam("menuName") String menuName,
            @RequestParam("menuDate") String menuDate,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String sessionId = getOrCreateSessionId(request, response);
            log.debug("좋아요/싫어요 조회 요청 - 원본 메뉴명: {}, 날짜: {}", menuName, menuDate);

            MenuLikeDislike likeDislike = menuLikeDislikeService.getLikeDislike(menuName, LocalDate.parse(menuDate));
            String currentVote = menuLikeDislikeService.getCurrentVote(menuName, LocalDate.parse(menuDate), sessionId);

            log.debug("조회 결과 - likeDislike: {}, currentVote: {}", likeDislike, currentVote);

            Map<String, Object> result = new HashMap<>();
            result.put("likeCount", likeDislike != null && likeDislike.getLikeCount() != null ? likeDislike.getLikeCount() : 0);
            result.put("dislikeCount", likeDislike != null && likeDislike.getDislikeCount() != null ? likeDislike.getDislikeCount() : 0);
            result.put("currentVote", currentVote);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("좋아요/싫어요 조회 오류 발생: ", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "조회 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResult);
        }
    }

@GetMapping("/debug/allLikeDislike")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAllLikeDislike() {
        try {
            List<MenuLikeDislike> allData = menuLikeDislikeService.getAllLikeDislike();
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (MenuLikeDislike item : allData) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", item.getId());
                map.put("menuDate", item.getMenuDate());
                map.put("menuName", item.getMenuName());
                map.put("likeCount", item.getLikeCount());
                map.put("dislikeCount", item.getDislikeCount());
                result.add(map);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("디버깅 조회 중 오류 발생: ", e);
            return ResponseEntity.status(500).build();
        }
    }
}
