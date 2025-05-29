package com.yeongjun.yeongjun.Security.service;


import com.yeongjun.yeongjun.Security.model.Role;
import com.yeongjun.yeongjun.Security.model.User;
import com.yeongjun.yeongjun.Security.repository.UserDAO;
import com.yeongjun.yeongjun.Security.util.JwtProvider;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;
    @Value("${recaptcha.secret-key}")
    private String recaptchaSecretKey;

    public UserService(UserDAO userDAO, PasswordEncoder passwordEncoder, JwtProvider jwtProvider, EmailService emailService) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.emailService = emailService;
    }

    // 회원가입 처리
    public boolean registerUser(User user) {
        List<String> errorMessages = new ArrayList<>();

        if (userDAO.getUserByUsername(user.getUsername()) != null) {
            errorMessages.add("이미 사용중인 아이디입니다.");
        }

        if (userDAO.getUserByEmail(user.getEmail()) != null) {
            errorMessages.add("이미 사용중인 이메일입니다.");
        }

        if (userDAO.getUserByNickname(user.getNickname()) != null) {
            errorMessages.add("이미 사용중인 닉네임입니다.");
        }

        if (!errorMessages.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errorMessages));
        }
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 비밀번호 암호화
        user.set_active(false); // 이메일 인증 전까지 비활성화
        user.setEmail_verified(false);
        user.setEmail_verification_token(UUID.randomUUID().toString());
        user.setEmail_verification_token_expiry(LocalDateTime.now().plusHours(24)); // 24시간 유효
        
        boolean success = userDAO.insertUser(user) > 0;
        if (success) {
            emailService.sendVerificationEmail(user.getEmail(), user.getEmail_verification_token());
        }
        return success;
    }

    // 이메일 인증 처리
    public boolean verifyEmail(String token) {
        User user = userDAO.getUserByVerificationToken(token);
        if (user != null && 
            user.getEmail_verification_token_expiry().isAfter(LocalDateTime.now())) {
            user.setEmail_verified(true);
            user.set_active(true);
            user.setEmail_verification_token(null);
            user.setEmail_verification_token_expiry(null);
            return userDAO.updateUser(user) > 0;
        }
        return false;
    }

    //reCaptcha 검증
    public boolean verifyRecaptcha(String recaptchaResponse) {
        String secret = recaptchaSecretKey;
        RestTemplate restTemplate = new RestTemplate();
        String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", secret);
        params.add("response", recaptchaResponse);

        ResponseEntity<Map> response = restTemplate.postForEntity(verifyUrl, params, Map.class);
        return (Boolean) response.getBody().get("success");
    }

    // 로그인 처리
    public String loginUser(String username, String password, boolean rememberMe) {
        User user = userDAO.getUserByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("아이디나 비밀번호가 올바르지 않습니다.");
        }
        if (!user.isEmail_verified()) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }
        // JWT 토큰 생성 (rememberMe에 따라 유효기간 조정)
        long validityInMilliseconds = rememberMe ? 30L * 24 * 60 * 60 * 1000 : 8L * 60 * 60 * 1000;
        return jwtProvider.createToken(user.getUsername(), user.getRole(), validityInMilliseconds);
    }

    public boolean isEmailExists(String email) {
        return userDAO.getUserByEmail(email) != null;
    }
}
