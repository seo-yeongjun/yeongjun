package com.yeongjun.yeongjun;


import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class BaseDAO<T> {

    @Autowired
    private SqlSession sqlSession;

    protected abstract String getNamespace();

    public int insert(String statement, T entity) {
        return sqlSession.insert(getNamespace() + "." + statement, entity);
    }

    public int update(String statement, T entity) {
        return sqlSession.update(getNamespace() + "." + statement, entity);
    }

    public T selectOne(String statement, Object parameter) {
        return sqlSession.selectOne(getNamespace() + "." + statement, parameter);
    }

    public List<T> selectList(String statement, Object parameter) {
        return sqlSession.selectList(getNamespace() + "." + statement, parameter);
    }
}
