package com.yeongjun.yeongjun.Security.repository;

import com.yeongjun.yeongjun.global.repository.BaseDAO;
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

    public User getUserByVerificationToken(String token) {
        return selectOne("getUserByVerificationToken", token);
    }

    public int insertUser(User user) {
        return insert("insertUser", user);
    }

    public int updateUser(User user) {
        return update("updateUser", user);
    }

    public User getUserByEmail(String email) {
        return selectOne("getUserByEmail", email);
    }

    public User getUserByNickname(String nickname) {
        return selectOne("getUserByNickname", nickname);
    }
}
