package com.example.mg.service;

import com.example.mg.dto.*;
import com.example.mg.common.R;

public interface AuthService {
    R<String> sendRegisterCode(String email, String ip);
    R<TokenResponse> register(RegisterRequest req, String ip, String ua);
    R<TokenResponse> login(LoginRequest req, String ip, String ua);
    R<TokenResponse> refresh(RefreshTokenRequest req, String ip, String ua);
    R<String> logout(String accessTokenJti, String refreshTokenId);
    R<String> sendResetCode(String email, String ip);
    R<String> resetPassword(ResetPasswordRequest req, String ip);
}
