package com.yeongjun.yeongjun.ajeGag.repository;

import com.yeongjun.yeongjun.ajeGag.model.Ajegag;
import com.yeongjun.yeongjun.babfullmenu.model.BabfullMenu;
import com.yeongjun.yeongjun.global.repository.BaseDAO;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class AjegagDAO extends BaseDAO<Ajegag> {
    @Override
    protected String getNamespace() {
        return "ajegag.ajegag";
    }

    public void selectAjegagList(Ajegag ajegag) {
        selectList("selectAjegagList","");
    }
}
