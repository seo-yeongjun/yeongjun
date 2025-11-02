package com.yeongjun.yeongjun.hyerin.repository;

import com.yeongjun.yeongjun.global.repository.BaseDAO;
import com.yeongjun.yeongjun.hyerin.entity.NyanghwagwaItemSetEntry;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NyanghwagwaItemSetEntryDAO extends BaseDAO<NyanghwagwaItemSetEntry> {
    @Override
    protected String getNamespace() {
        return "nyanghwagwa.itemSetEntry";
    }

    public List<NyanghwagwaItemSetEntry> selectAllEntries() {
        return selectList("selectAllEntries", null);
    }

    public List<NyanghwagwaItemSetEntry> selectEntriesBySetId(Long setId) {
        return selectList("selectEntriesBySetId", setId);
    }

    public int insertEntry(NyanghwagwaItemSetEntry entry) {
        return insert("insertEntry", entry);
    }

    public int updateEntry(NyanghwagwaItemSetEntry entry) {
        return update("updateEntry", entry);
    }

    public int deleteEntry(NyanghwagwaItemSetEntry entry) {
        return delete("deleteEntry", entry);
    }

    public int deleteEntriesBySetId(Long setId) {
        return sqlSessionTemplate.delete(getNamespace() + ".deleteEntriesBySetId", setId);
    }
}
