package com.example.mg.dto;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshTokenId;
    private String refreshToken;
}
