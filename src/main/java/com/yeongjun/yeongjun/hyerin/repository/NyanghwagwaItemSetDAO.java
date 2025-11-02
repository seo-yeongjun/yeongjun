package com.yeongjun.yeongjun.hyerin.repository;

import com.yeongjun.yeongjun.global.repository.BaseDAO;
import com.yeongjun.yeongjun.hyerin.entity.NyanghwagwaItemSet;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NyanghwagwaItemSetDAO extends BaseDAO<NyanghwagwaItemSet> {
    @Override
    protected String getNamespace() {
        return "nyanghwagwa.itemSet";
    }

    public List<NyanghwagwaItemSet> selectAllSets() {
        return selectList("selectAllSets", null);
    }

    public NyanghwagwaItemSet selectSetById(Long setId) {
        return selectOne("selectSetById", setId);
    }

    public int insertSet(NyanghwagwaItemSet itemSet) {
        return insert("insertSet", itemSet);
    }

    public int updateSet(NyanghwagwaItemSet itemSet) {
        return update("updateSet", itemSet);
    }

    public int deleteSet(Long setId) {
        NyanghwagwaItemSet param = new NyanghwagwaItemSet();
        param.setSet_id(setId);
        return delete("deleteSet", param);
    }
}
