package com.yeongjun.yeongjun.transactions.service;

import com.yeongjun.yeongjun.babfullmenu.model.BabfullMenu;
import com.yeongjun.yeongjun.babfullmenu.repository.BabfullMenuDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BabfullMenuService {

    private final BabfullMenuDAO babfullMenuDAO;

    /**
     * 오늘의 메뉴를 조회합니다.
     *
     * @return 오늘의 BabfullMenu 리스트
     */
    @Transactional(readOnly = true)
    public List<BabfullMenu> getRelevantMenu() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        LocalDate dateToUse;

        if (dayOfWeek == DayOfWeek.SATURDAY) {
            // 토요일인 경우, 하루를 빼서 금요일을 사용
            dateToUse = today.minusDays(1);
            log.debug("오늘은 토요일입니다. 금요일({})을 사용하여 메뉴를 조회합니다.", dateToUse);
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            // 일요일인 경우, 이틀을 빼서 금요일을 사용
            dateToUse = today.minusDays(2);
            log.debug("오늘은 일요일입니다. 금요일({})을 사용하여 메뉴를 조회합니다.", dateToUse);
        } else {
            // 평일인 경우, 오늘 날짜를 사용
            dateToUse = today;
            log.debug("오늘은 평일입니다. 오늘 날짜를 사용하여 메뉴를 조회합니다: {}", dateToUse);
        }

        return babfullMenuDAO.selectMenuByDate(dateToUse);
    }

    /**
     * BabfullMenu 리스트를 데이터베이스에 삽입합니다.
     *
     * @param babfullMenus 삽입할 BabfullMenu 리스트
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertBabfullMenus(List<BabfullMenu> babfullMenus) throws Exception {
        for (BabfullMenu menu : babfullMenus) {
            log.debug("Inserting menu: {}", menu);
            babfullMenuDAO.insertBabfullMenu(menu);
        }
    }

    public LocalDate parseMonthDay(String dateString) {
        // "12월16일" 과 같은 문자열에서 월, 일을 추출
        // (공백 제거와 특수문자 제거는 이미 위에서 끝냈다고 가정)
        try {
            int idxOfMonth = dateString.indexOf("월");
            int idxOfDay = dateString.indexOf("일");

            int month = Integer.parseInt(dateString.substring(0, idxOfMonth));
            int day = Integer.parseInt(dateString.substring(idxOfMonth + 1, idxOfDay));

            // 해는 현재 연도로 가정(필요시 다른 로직으로 보정)
            int year = Year.now().getValue();

            return LocalDate.of(year, month, day);
        } catch (NumberFormatException | DateTimeParseException e) {
            // 파싱 실패 시 null 리턴 등 예외처리
            return null;
        }
    }
}
