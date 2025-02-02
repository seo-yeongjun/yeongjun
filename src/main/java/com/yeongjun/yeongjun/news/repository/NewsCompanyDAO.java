package com.yeongjun.yeongjun.news.repository;

import com.yeongjun.yeongjun.global.repository.BaseDAO;
import com.yeongjun.yeongjun.news.entity.NewsCompany;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NewsCompanyDAO extends BaseDAO<NewsCompany> {
    @Override
    protected String getNamespace() {
        return "news.newsCompany";
    }
    public List<NewsCompany> selectAllCompany() {
        return selectList("selectAllCompany", null);
    }
}
