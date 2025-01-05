package com.yeongjun.yeongjun.global.repository;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class BaseDAO<T> {

    @Autowired
    protected SqlSessionTemplate sqlSessionTemplate;

    protected abstract String getNamespace();

    public int insert(String statement, T entity) {
        return sqlSessionTemplate.insert(getNamespace() + "." + statement, entity);
    }

    public int update(String statement, T entity) {
        return sqlSessionTemplate.update(getNamespace() + "." + statement, entity);
    }

    public int delete(String statement, T entity) {
        return sqlSessionTemplate.delete(getNamespace() + "." + statement, entity);
    }

    public T selectOne(String statement, Object parameter) {
        return sqlSessionTemplate.selectOne(getNamespace() + "." + statement, parameter);
    }

    public List<T> selectList(String statement, Object parameter) {
        return sqlSessionTemplate.selectList(getNamespace() + "." + statement, parameter);
    }
}
