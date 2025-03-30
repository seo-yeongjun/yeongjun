package com.yeongjun.yeongjun.coffeeGame.repository;

import com.yeongjun.yeongjun.coffeeGame.entity.CoffeeGameRecord;
import com.yeongjun.yeongjun.global.repository.BaseDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CoffeeGameStatDAO extends BaseDAO<CoffeeGameRecord> {
    @Override
    protected String getNamespace() {
        return "coffeeGame.coffeeGameRecord";
    }

    public List<CoffeeGameRecord> getAllRecords() {
        return selectList("selectAllRecords", null);
    }

    public void saveRecord(CoffeeGameRecord record) {
        insert("insertRecord", record);
    }

    public int getTotalCostByLoser(String loser_name) {
         return selectOne("selectTotalCostByLoser", loser_name).getCost();
    }
}

