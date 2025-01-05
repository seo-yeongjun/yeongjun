package com.yeongjun.yeongjun.babfullmenu.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class BabfullMenu {

    private Integer menu_id;
    private LocalDate start_dt;
    private LocalDate end_dt;
    private LocalDate menu_dt;
    private String provider;
    private String morning_menu1;
    private String morning_menu2;
    private String morning_menu3;
    private String morning_menu4;
    private String morning_menu5;
    private String morning_menu6;
    private String morning_menu7;
    private String morning_menu8;
    private String morning_menu9;
    private String lunch_menu1;
    private String lunch_menu2;
    private String lunch_menu3;
    private String lunch_menu4;
    private String lunch_menu5;
    private String lunch_menu6;
    private String lunch_menu7;
    private String lunch_menu8;
    private String lunch_menu9;

    /**
     * 각 List의 길이만큼 morning_menu 및 lunch_menu에 값을 할당하여 BabfullMenu 객체 리스트를 생성
     *
     * @param startDt          메뉴 시작일
     * @param endDt            메뉴 종료일
     * @param provider         공급자 정보
     * @param lunchDayList1    첫 번째 날의 점심 메뉴 리스트
     * @param lunchDayList2    두 번째 날의 점심 메뉴 리스트
     * @param lunchDayList3    세 번째 날의 점심 메뉴 리스트
     * @param lunchDayList4    네 번째 날의 점심 메뉴 리스트
     * @param lunchDayList5    다섯 번째 날의 점심 메뉴 리스트
     * @param morningDayList1  첫 번째 날의 아침 메뉴 리스트
     * @param morningDayList2  두 번째 날의 아침 메뉴 리스트
     * @param morningDayList3  세 번째 날의 아침 메뉴 리스트
     * @param morningDayList4  네 번째 날의 아침 메뉴 리스트
     * @param morningDayList5  다섯 번째 날의 아침 메뉴 리스트
     * @return BabfullMenu 객체 리스트
     */
    public List<BabfullMenu> convertToBabfullMenuList(
            LocalDate startDt,
            LocalDate endDt,
            String provider,
            List<String> lunchDayList1,
            List<String> lunchDayList2,
            List<String> lunchDayList3,
            List<String> lunchDayList4,
            List<String> lunchDayList5,
            List<String> morningDayList1,
            List<String> morningDayList2,
            List<String> morningDayList3,
            List<String> morningDayList4,
            List<String> morningDayList5
    ) {
        List<BabfullMenu> menuList = new ArrayList<>();

        // 점심 및 아침 메뉴 리스트를 리스트의 리스트로 관리
        List<List<String>> lunchLists = Arrays.asList(lunchDayList1, lunchDayList2, lunchDayList3, lunchDayList4, lunchDayList5);
        List<List<String>> morningLists = Arrays.asList(morningDayList1, morningDayList2, morningDayList3, morningDayList4, morningDayList5);

        for (int i = 0; i < lunchLists.size(); i++) {
            BabfullMenu menu = new BabfullMenu();
            menu.setStart_dt(startDt);
            menu.setEnd_dt(endDt);

            // 메뉴 날짜 설정 (시작일부터 하루씩 증가)
            LocalDate menuDate = startDt.plusDays(i);
            menu.setMenu_dt(menuDate);

            menu.setProvider(provider);

            // 아침 메뉴 설정
            List<String> morningList = morningLists.get(i);
            setMorningMenus(menu, morningList);

            // 점심 메뉴 설정
            List<String> lunchList = lunchLists.get(i);
            setLunchMenus(menu, lunchList);

            menuList.add(menu);
        }

        return menuList;
    }

    /**
     * 아침 메뉴 리스트를 BabfullMenu 객체에 설정
     *
     * @param menu        BabfullMenu 객체
     * @param morningList 아침 메뉴 리스트
     */
    private void setMorningMenus(BabfullMenu menu, List<String> morningList) {
        for (int j = 0; j < morningList.size() && j < 9; j++) {
            switch (j) {
                case 0:
                    menu.setMorning_menu1(morningList.get(j));
                    break;
                case 1:
                    menu.setMorning_menu2(morningList.get(j));
                    break;
                case 2:
                    menu.setMorning_menu3(morningList.get(j));
                    break;
                case 3:
                    menu.setMorning_menu4(morningList.get(j));
                    break;
                case 4:
                    menu.setMorning_menu5(morningList.get(j));
                    break;
                case 5:
                    menu.setMorning_menu6(morningList.get(j));
                    break;
                case 6:
                    menu.setMorning_menu7(morningList.get(j));
                    break;
                case 7:
                    menu.setMorning_menu8(morningList.get(j));
                    break;
                case 8:
                    menu.setMorning_menu9(morningList.get(j));
                    break;
                default:
                    // 최대 9개까지만 설정
                    break;
            }
        }
    }

    /**
     * 점심 메뉴 리스트를 BabfullMenu 객체에 설정
     *
     * @param menu     BabfullMenu 객체
     * @param lunchList 점심 메뉴 리스트
     */
    private void setLunchMenus(BabfullMenu menu, List<String> lunchList) {
        for (int j = 0; j < lunchList.size() && j < 9; j++) {
            switch (j) {
                case 0:
                    menu.setLunch_menu1(lunchList.get(j));
                    break;
                case 1:
                    menu.setLunch_menu2(lunchList.get(j));
                    break;
                case 2:
                    menu.setLunch_menu3(lunchList.get(j));
                    break;
                case 3:
                    menu.setLunch_menu4(lunchList.get(j));
                    break;
                case 4:
                    menu.setLunch_menu5(lunchList.get(j));
                    break;
                case 5:
                    menu.setLunch_menu6(lunchList.get(j));
                    break;
                case 6:
                    menu.setLunch_menu7(lunchList.get(j));
                    break;
                case 7:
                    menu.setLunch_menu8(lunchList.get(j));
                    break;
                case 8:
                    menu.setLunch_menu9(lunchList.get(j));
                    break;
                default:
                    // 최대 9개까지만 설정
                    break;
            }
        }
    }

    public Date getFormatedMenuDt() {
        return Date.from(menu_dt.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
