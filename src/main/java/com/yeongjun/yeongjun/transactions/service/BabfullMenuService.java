package com.yeongjun.yeongjun.transactions.service;

import com.yeongjun.yeongjun.babfullmenu.model.BabfullMenu;
import com.yeongjun.yeongjun.babfullmenu.repository.BabfullMenuDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.time.DayOfWeek;
import java.time.LocalDate;

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
        LocalDate today = LocalDate.now();
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
}
