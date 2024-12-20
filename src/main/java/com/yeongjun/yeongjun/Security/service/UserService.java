package com.yeongjun.yeongjun.Security.service;

import com.yeongjun.yeongjun.Security.model.Role;
import com.yeongjun.yeongjun.Security.model.User;
import com.yeongjun.yeongjun.Security.repository.UserDAO;
import com.yeongjun.yeongjun.Security.util.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public UserService(UserDAO userDAO, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    // 회원가입 처리
    public boolean registerUser(User user) {
        if (userDAO.getUserByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 비밀번호 암호화
        user.set_active(true); // 활성화 상태로 설정
        return userDAO.insertUser(user) > 0; // 저장 성공 여부 반환
    }

    // 로그인 처리
    public String loginUser(String username, String password) {
        User user = userDAO.getUserByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("아이디나 비밀번호가 올바르지 않습니다.");
        }
        // JWT 토큰 생성
        return jwtProvider.createToken(user.getUsername(), user.getRole());
    }
}
