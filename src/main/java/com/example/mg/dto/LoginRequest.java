package com.example.mg.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private Boolean rememberMe;
    private String username;
}
