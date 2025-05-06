package com.yeongjun.yeongjun.magicBook.repository;

import com.yeongjun.yeongjun.global.repository.BaseDAO;
import com.yeongjun.yeongjun.magicBook.entity.MagicBookEntry;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MagicBookDAO extends BaseDAO<MagicBookEntry> {
    @Override
    protected String getNamespace() {
        return "magicBook.magicBook";
    }

    public List<MagicBookEntry> selectAllByCategory(String category) {
        return selectList("selectByCategory", category);
    }

    public MagicBookEntry selectById(Long id) {
        return selectOne("selectById",id);
    }
}
