package com.yeongjun.yeongjun.babfullmenu.repository;

import com.yeongjun.yeongjun.babfullmenu.model.BabfullMenu;
import com.yeongjun.yeongjun.global.repository.BaseDAO;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class BabfullMenuDAO extends BaseDAO<BabfullMenu> {
    @Override
    protected String getNamespace() {
        return "babfullMenu.babfullMenu";
    }

    /**
     * 오늘의 메뉴를 조회합니다.
     *
     * @return 오늘의 BabfullMenu 리스트
     */
    public List<BabfullMenu> selectBabfullMenuToday() {
        return selectList("selectBabfullMenuToday", null);
    }

    /**
     * BabfullMenu를 데이터베이스에 삽입합니다.
     *
     * @param babfullMenu 삽입할 BabfullMenu 객체
     */
    public void insertBabfullMenu(BabfullMenu babfullMenu) {
        insert("insertBabfullMenu", babfullMenu);
    }

    public List<BabfullMenu> selectMenuByDate(LocalDate date) {
        return selectList("selectMenuByDate", date);
    }
}
