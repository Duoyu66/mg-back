package com.example.mg.controller;

import com.example.mg.common.R;
import com.example.mg.dto.*;
import com.example.mg.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "认证", description = "登录注册与令牌接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/sendRegisterCode")
    @Operation(summary = "发送注册邮箱验证码")
    public R<String> sendRegisterCode(@RequestBody SendEmailCodeRequest req, HttpServletRequest request) {
        String ip = clientIp(request);
        return authService.sendRegisterCode(req.getEmail(), ip);
    }
    @PostMapping("/register")
    @Operation(summary = "邮箱注册")
    public R<TokenResponse> register(@RequestBody RegisterRequest req, HttpServletRequest request, @RequestHeader(value = "User-Agent", required = false) String ua) {
        String ip = clientIp(request);
        return authService.register(req, ip, ua);
    }
    @PostMapping("/login")
    @Operation(summary = "邮箱密码登录")
    public R<TokenResponse> login(@RequestBody LoginRequest req, HttpServletRequest request, @RequestHeader(value = "User-Agent", required = false) String ua) {
        String ip = clientIp(request);
        return authService.login(req, ip, ua);
    }
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌")
    public R<TokenResponse> refresh(@RequestBody RefreshTokenRequest req, HttpServletRequest request, @RequestHeader(value = "User-Agent", required = false) String ua) {
        String ip = clientIp(request);
        return authService.refresh(req, ip, ua);
    }
    @PostMapping("/logout")
    @Operation(summary = "注销令牌")
    public R<String> logout(@RequestParam(required = false) String accessJti, @RequestParam String refreshTokenId) {
        return authService.logout(accessJti, refreshTokenId);
    }
    @PostMapping("/forgot/send")
    @Operation(summary = "发送重置密码验证码")
    public R<String> sendResetCode(@RequestBody SendEmailCodeRequest req, HttpServletRequest request) {
        String ip = clientIp(request);
        return authService.sendResetCode(req.getEmail(), ip);
    }
    @PostMapping("/forgot/reset")
    @Operation(summary = "邮箱验证码重置密码")
    public R<String> resetPassword(@RequestBody ResetPasswordRequest req, HttpServletRequest request) {
        String ip = clientIp(request);
        return authService.resetPassword(req, ip);
    }
    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }
}
