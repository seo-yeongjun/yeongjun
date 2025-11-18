package com.yeongjun.yeongjun.babfullmenu.repository;

import com.yeongjun.yeongjun.babfullmenu.model.MenuLikeDislike;
import com.yeongjun.yeongjun.global.repository.BaseDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MenuLikeDislikeDAO extends BaseDAO<MenuLikeDislike> {
    @Override
    protected String getNamespace() {
        return "babfullMenu.menuLikeDislike";
    }

    public MenuLikeDislike selectByNormalizedMenuName(String normalizedMenuName) {
        return selectOne("selectByNormalizedMenuName", normalizedMenuName);
    }

    public int insertMenuLikeDislike(MenuLikeDislike menuLikeDislike) {
        return insert("insertMenuLikeDislike", menuLikeDislike);
    }

    public int updateLikeCount(MenuLikeDislike menuLikeDislike) {
        return update("updateLikeCount", menuLikeDislike);
    }

    public int updateDislikeCount(MenuLikeDislike menuLikeDislike) {
        return update("updateDislikeCount", menuLikeDislike);
    }

    public List<MenuLikeDislike> selectAll() {
        return selectList("selectAll", null);
    }
}

