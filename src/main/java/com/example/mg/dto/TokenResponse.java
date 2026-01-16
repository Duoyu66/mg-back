package com.example.mg.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private Long accessTokenExpiresInSeconds;
    private String refreshTokenId;
    private String refreshToken;
    private Long refreshTokenExpiresInSeconds;
}
