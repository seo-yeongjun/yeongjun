package com.yeongjun.yeongjun.Security.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String nickname;
}
