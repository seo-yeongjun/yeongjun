package com.yeongjun.yeongjun.hyerin.repository;

import com.yeongjun.yeongjun.global.repository.BaseDAO;
import com.yeongjun.yeongjun.hyerin.entity.NyanghwagwaItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NyanghwagwaItemDAO extends BaseDAO<NyanghwagwaItem> {
    @Override
    protected String getNamespace() {
        return "nyanghwagwa.item";
    }

    public List<NyanghwagwaItem> selectAllItems() {
        return selectList("selectAllItems", null);
    }

    public NyanghwagwaItem selectItemById(Long itemId) {
        return selectOne("selectItemById", itemId);
    }

    public int insertItem(NyanghwagwaItem item) {
        return insert("insertItem", item);
    }

    public int updateItem(NyanghwagwaItem item) {
        return update("updateItem", item);
    }

    public int deleteItem(Long itemId) {
        NyanghwagwaItem param = new NyanghwagwaItem();
        param.setItem_id(itemId);
        return delete("deleteItem", param);
    }

    public int updateItemQuantity(NyanghwagwaItem item) {
        return update("updateItemQuantity", item);
    }
}
