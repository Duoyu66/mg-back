package com.example.mg.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:mg-back-secret-key-change-in-production}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private long expiration;

    /**
     * 生成Token
     */
    public String generateToken(String userId, String username) {
        try {
            Date now = new Date();
            Date expireDate = new Date(now.getTime() + expiration);

            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withSubject(userId)
                    .withClaim("username", username)
                    .withIssuedAt(now)
                    .withExpiresAt(expireDate)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("生成Token失败", exception);
        }
    }

    /**
     * 验证Token并获取用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token验证失败", exception);
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            return false;
        }
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("username").asString();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token验证失败", exception);
        }
    }
}

