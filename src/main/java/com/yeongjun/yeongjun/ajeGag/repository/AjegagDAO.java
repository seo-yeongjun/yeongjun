package com.yeongjun.yeongjun.ajeGag.repository;

import com.yeongjun.yeongjun.ajeGag.model.Ajegag;
import com.yeongjun.yeongjun.global.repository.BaseDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AjegagDAO extends BaseDAO<Ajegag> {
    @Override
    protected String getNamespace() {
        return "ajegag.ajegag";
    }

    public List<Ajegag> selectAjegagList() {
       return selectList("selectAjegagList","");
    }

    public Ajegag findById(Long id) {
        return selectOne("findById", id);
    }
}
