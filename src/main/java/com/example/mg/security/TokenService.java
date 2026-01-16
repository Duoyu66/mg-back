package com.example.mg.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Instant;
import java.util.UUID;

public class TokenService {
    private final Algorithm algorithm;
    private final long accessSeconds;
    public TokenService(String secret, long accessSeconds) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.accessSeconds = accessSeconds;
    }
    public String issueAccess(String userId, String deviceFp) {
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessSeconds);
        return JWT.create()
                .withSubject(userId)
                .withJWTId(jti)
                .withClaim("dfp", deviceFp)
                .withIssuedAt(java.util.Date.from(now))
                .withExpiresAt(java.util.Date.from(exp))
                .sign(algorithm);
    }
}
