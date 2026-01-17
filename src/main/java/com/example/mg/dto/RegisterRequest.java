package com.example.mg.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String nickName;
    private String code;
    private String username;
    private Boolean rememberMe;
}
