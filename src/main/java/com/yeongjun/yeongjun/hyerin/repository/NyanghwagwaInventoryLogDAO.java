package com.yeongjun.yeongjun.hyerin.repository;

import com.yeongjun.yeongjun.global.repository.BaseDAO;
import com.yeongjun.yeongjun.hyerin.entity.NyanghwagwaInventoryLog;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class NyanghwagwaInventoryLogDAO extends BaseDAO<NyanghwagwaInventoryLog> {
    @Override
    protected String getNamespace() {
        return "nyanghwagwa.inventoryLog";
    }

    public List<NyanghwagwaInventoryLog> selectRecentLogs(Integer limit) {
        return selectList("selectRecentLogs", limit);
    }

    public int insertLog(NyanghwagwaInventoryLog log) {
        return insert("insertLog", log);
    }

    public int countProduceLogsBySetId(Long setId) {
        Integer result = sqlSessionTemplate.selectOne(getNamespace() + ".countProduceLogsBySetId", setId);
        return result != null ? result : 0;
    }

    public Map<String, Object> selectLatestActivitySummary() {
        return sqlSessionTemplate.selectOne(getNamespace() + ".selectLatestActivitySummary");
    }
}
