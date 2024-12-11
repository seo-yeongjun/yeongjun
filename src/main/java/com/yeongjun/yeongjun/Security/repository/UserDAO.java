package com.yeongjun.yeongjun.Security.repository;

import com.yeongjun.yeongjun.BaseDAO;
import com.yeongjun.yeongjun.Security.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAO extends BaseDAO<User> {
    @Override
    protected String getNamespace() {
        return "login.login";
    }

    public User getUserByUsername(String username) {
        return selectOne("getUserById", username);
    }

    public int insertUser(User user) {
        return insert("insertUser", user);
    }
}
