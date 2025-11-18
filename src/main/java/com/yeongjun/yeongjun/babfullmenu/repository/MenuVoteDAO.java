package com.yeongjun.yeongjun.babfullmenu.repository;

import com.yeongjun.yeongjun.babfullmenu.model.MenuVote;
import com.yeongjun.yeongjun.global.repository.BaseDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MenuVoteDAO extends BaseDAO<MenuVote> {
    @Override
    protected String getNamespace() {
        return "babfullMenu.menuVote";
    }

    public MenuVote selectByNormalizedMenuNameAndSessionId(String normalizedMenuName, String sessionId) {
        MenuVote param = new MenuVote();
        param.setNormalizedMenuName(normalizedMenuName);
        param.setSessionId(sessionId);
        return selectOne("selectByNormalizedMenuNameAndSessionId", param);
    }

    public int insertMenuVote(MenuVote menuVote) {
        return insert("insertMenuVote", menuVote);
    }

    public int deleteMenuVote(MenuVote menuVote) {
        return delete("deleteMenuVote", menuVote);
    }

    public List<MenuVote> selectRecentVotesBySessionId(String sessionId) {
        return selectList("selectRecentVotesBySessionId", sessionId);
    }
}

