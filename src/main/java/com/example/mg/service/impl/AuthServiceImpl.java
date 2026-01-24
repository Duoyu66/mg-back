package com.example.mg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mg.common.R;
import com.example.mg.dto.*;
import com.example.mg.entity.*;
import com.example.mg.mapper.*;
import com.example.mg.security.DeviceFingerprint;
import com.example.mg.security.PasswordPolicy;
import com.example.mg.security.TokenService;
import com.example.mg.service.AuthService;
import lombok.RequiredArgsConstructor;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.mg.util.InMemoryKVStore;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordHistoryMapper passwordHistoryMapper;
    private final AuditLogMapper auditLogMapper;
    private final InMemoryKVStore kvStore;
    @Value("${security.jwt.secret:change-me}")
    private String jwtSecret;
    @Value("${security.session.accessSeconds:1200}")
    private long accessSeconds;
    @Value("${security.session.refreshSeconds:604800}")
    private long refreshSeconds;
    @Value("${security.session.rememberRefreshSeconds:1209600}")
    private long rememberRefreshSeconds;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public R<String> sendRegisterCode(String email, String ip) {
        String lastKey = "email:send:last:" + email;
        String dayKey = "email:send:count:" + email + ":" + LocalDateTime.now().toLocalDate().toString();
        String last = getVal(lastKey);
//        if (last != null) return R.failed("发送过于频繁");
//        String count = getVal(dayKey);
//        if (count != null && Integer.parseInt(count) >= 10) return R.failed("当日发送次数已达上限");
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        setVal("email:code:register:" + email, code, 5 * 60);
        log.info("验证码是>{}",code);
        setVal(lastKey, "1", 60);
        incVal(dayKey);
        expireVal(dayKey, Duration.ofDays(1));
        audit("REGISTER_CODE", null, 1, "ok", ip, null);
        return R.success("验证码已发送");
    }

    @Override
    public R<TokenResponse> register(RegisterRequest req, String ip, String ua) {
        String ipKey = "ip:register:" + ip + ":" + LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
//        String ipCount = getVal(ipKey);
//        if (ipCount != null && Integer.parseInt(ipCount) >= 5) return R.failed("该IP注册过于频繁");
        String email = req.getEmail();
//        String cachedCode = getVal("email:code:register:" + email);
//        if (cachedCode == null || !cachedCode.equals(req.getCode())) return R.failed("验证码错误或已过期");
//        UserEntity exists = userMapper.selectOne(new QueryWrapper<UserEntity>().eq("email", email));
//        if (exists != null) return R.failed("邮箱已被注册");
//        if (!PasswordPolicy.valid(req.getPassword())) return R.failed("密码不符合强度要求");
        String id = Long.toString(IdUtil.getSnowflakeNextId());
        UserEntity user = new UserEntity();
        user.setId(id);
        String prefix = email.split("@")[0];
        user.setUsername(prefix);
        user.setNickName(req.getNickName() == null || req.getNickName().isEmpty() ? prefix : req.getNickName());
        user.setAccount(prefix);
        user.setPassword(encoder.encode(req.getPassword()));
        user.setEmail(email);
        user.setSchool(null);
        user.setAvatar(null);
        user.setJob(null);
        user.setGender(0);
        user.setMobile(null);
        user.setStatus(1);
        user.setVipType("free");
        user.setVipStartTime(null);
        user.setVipEndTime(null);
        user.setExp(0);
        user.setPoints(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setRegisterIp(ip);
        userMapper.insert(user);
        PasswordHistoryEntity ph = new PasswordHistoryEntity();
        ph.setId(UUID.randomUUID().toString());
        ph.setUserId(id);
        ph.setPasswordHash(user.getPassword());
        ph.setCreatedAt(LocalDateTime.now());
        passwordHistoryMapper.insert(ph);
        String deviceFp = DeviceFingerprint.fp(ip, ua);
        TokenService ts = new TokenService(jwtSecret, accessSeconds);
        String access = ts.issueAccess(id, deviceFp);
        String rt = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String rtId = UUID.randomUUID().toString();
        RefreshTokenEntity rte = new RefreshTokenEntity();
        rte.setId(rtId);
        rte.setUserId(id);
        rte.setTokenHash(encoder.encode(rt));
        rte.setDeviceFp(deviceFp);
        long rs = req.getRememberMe() != null && Boolean.TRUE.equals(req.getRememberMe()) ? rememberRefreshSeconds : refreshSeconds;
        rte.setExpiresAt(LocalDateTime.now().plusSeconds(rs));
        rte.setCreatedAt(LocalDateTime.now());
        rte.setRevoked(0);
        refreshTokenMapper.insert(rte);
        incVal(ipKey);
        expireVal(ipKey, Duration.ofHours(1));
        delVal("email:code:register:" + email);
        audit("REGISTER", id, 1, "ok", ip, ua);
        
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        TokenResponse tr = TokenResponse.builder()
                .accessToken(access)
                .accessTokenExpiresInSeconds(accessSeconds)
                .refreshTokenId(rtId)
                .refreshToken(rt)
                .refreshTokenExpiresInSeconds(rs)
                .user(userDTO)
                .build();
        return R.success(tr);
    }

    @Override
    public R<TokenResponse> login(LoginRequest req, String ip, String ua) {
        UserEntity user = userMapper.selectOne(new QueryWrapper<UserEntity>().eq("email", req.getEmail()));
        if (user == null) return R.failed("账号不存在");
        String lockKey = "user:lock:" + user.getId();
        String lockedUntil = getVal(lockKey);
        if (lockedUntil != null) return R.failed("账号已锁定");
        boolean ok = encoder.matches(req.getPassword(), user.getPassword());
        if (!ok) {
            String failedKey = "user:login:failed:" + user.getId();
            Long c = incVal(failedKey);
            expireVal(failedKey, Duration.ofMinutes(15));
            if (c != null && c >= 5) {
                setVal(lockKey, "1", 15 * 60);
            }
            audit("LOGIN", user.getId(), 0, "bad_password", ip, ua);
            return R.failed("密码错误");
        }
        delVal("user:login:failed:" + user.getId());
        String deviceFp = DeviceFingerprint.fp(ip, ua);
        TokenService ts = new TokenService(jwtSecret, accessSeconds);
        String access = ts.issueAccess(user.getId(), deviceFp);
        String rt = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String rtId = UUID.randomUUID().toString();
        RefreshTokenEntity rte = new RefreshTokenEntity();
        rte.setId(rtId);
        rte.setUserId(user.getId());
        rte.setTokenHash(encoder.encode(rt));
        rte.setDeviceFp(deviceFp);
        long rs = Boolean.TRUE.equals(req.getRememberMe()) ? rememberRefreshSeconds : refreshSeconds;
        rte.setExpiresAt(LocalDateTime.now().plusSeconds(rs));
        rte.setCreatedAt(LocalDateTime.now());
        rte.setRevoked(0);
        refreshTokenMapper.insert(rte);
        audit("LOGIN", user.getId(), 1, "ok", ip, ua);
        
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .vipType(user.getVipType())
                .nickName(user.getNickName())
                .build();

        TokenResponse tr = TokenResponse.builder()
                .accessToken(access)
                .accessTokenExpiresInSeconds(accessSeconds)
                .refreshTokenId(rtId)
                .refreshToken(rt)
                .refreshTokenExpiresInSeconds(rs)
                .user(userDTO)
                .build();
        return R.success(tr);
    }

    @Override
    public R<TokenResponse> refresh(RefreshTokenRequest req, String ip, String ua) {
        RefreshTokenEntity rte = refreshTokenMapper.selectById(req.getRefreshTokenId());
        if (rte == null) return R.failed("非法令牌");
        if (rte.getRevoked() != null && rte.getRevoked() == 1) return R.failed("令牌已失效");
        if (rte.getExpiresAt().isBefore(LocalDateTime.now())) return R.failed("令牌过期");
        String deviceFp = DeviceFingerprint.fp(ip, ua);
        if (rte.getDeviceFp() != null && !rte.getDeviceFp().equals(deviceFp)) return R.failed("设备不匹配");
        if (!encoder.matches(req.getRefreshToken(), rte.getTokenHash())) return R.failed("令牌不匹配");
        rte.setRevoked(1);
        refreshTokenMapper.updateById(rte);
        TokenService ts = new TokenService(jwtSecret, accessSeconds);
        String access = ts.issueAccess(rte.getUserId(), deviceFp);
        String newRt = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String newRtId = UUID.randomUUID().toString();
        RefreshTokenEntity nr = new RefreshTokenEntity();
        nr.setId(newRtId);
        nr.setUserId(rte.getUserId());
        nr.setTokenHash(encoder.encode(newRt));
        nr.setDeviceFp(deviceFp);
        nr.setExpiresAt(LocalDateTime.now().plusSeconds(refreshSeconds));
        nr.setCreatedAt(LocalDateTime.now());
        nr.setRevoked(0);
        refreshTokenMapper.insert(nr);
        audit("REFRESH", rte.getUserId(), 1, "ok", ip, ua);
        TokenResponse tr = TokenResponse.builder()
                .accessToken(access)
                .accessTokenExpiresInSeconds(accessSeconds)
                .refreshTokenId(newRtId)
                .refreshToken(newRt)
                .refreshTokenExpiresInSeconds(refreshSeconds)
                .build();
        return R.success(tr);
    }

    @Override
    public R<String> logout(String accessTokenJti, String refreshTokenId) {
        refreshTokenMapper.updateById(buildRevoked(refreshTokenId));
        if (accessTokenJti != null) {
            setVal("blacklist:access:" + accessTokenJti, "1", accessSeconds);
        }
        return R.success("ok");
    }

    @Override
    public R<String> sendResetCode(String email, String ip) {
        String lastKey = "email:send:last:reset:" + email;
        String dayKey = "email:send:count:reset:" + email + ":" + LocalDateTime.now().toLocalDate().toString();
        String last = getVal(lastKey);
        if (last != null) return R.failed("发送过于频繁");
        String count = getVal(dayKey);
        if (count != null && Integer.parseInt(count) >= 10) return R.failed("当日发送次数已达上限");
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        setVal("email:code:reset:" + email, code, 5 * 60);
        setVal(lastKey, "1", 60);
        incVal(dayKey);
        expireVal(dayKey, Duration.ofDays(1));
        audit("RESET_CODE", null, 1, "ok", ip, null);
        return R.success("验证码已发送");
    }

    @Override
    public R<String> resetPassword(ResetPasswordRequest req, String ip) {
        String email = req.getEmail();
        String cachedCode = getVal("email:code:reset:" + email);
        if (cachedCode == null || !cachedCode.equals(req.getCode())) return R.failed("验证码错误或已过期");
        if (!PasswordPolicy.valid(req.getNewPassword())) return R.failed("密码不符合强度要求");
        UserEntity user = userMapper.selectOne(new QueryWrapper<UserEntity>().eq("email", email));
        if (user == null) return R.failed("账号不存在");
        QueryWrapper<PasswordHistoryEntity> qw = new QueryWrapper<PasswordHistoryEntity>()
                .eq("user_id", user.getId())
                .orderByDesc("created_at")
                .last("limit 3");
        java.util.List<PasswordHistoryEntity> list = passwordHistoryMapper.selectList(qw);
        for (PasswordHistoryEntity ph : list) {
            if (encoder.matches(req.getNewPassword(), ph.getPasswordHash())) return R.failed("不得重复使用最近3次密码");
        }
        String hash = encoder.encode(req.getNewPassword());
        user.setPassword(hash);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        PasswordHistoryEntity ph = new PasswordHistoryEntity();
        ph.setId(UUID.randomUUID().toString());
        ph.setUserId(user.getId());
        ph.setPasswordHash(hash);
        ph.setCreatedAt(LocalDateTime.now());
        passwordHistoryMapper.insert(ph);
        delVal("email:code:reset:" + email);
        audit("RESET_PASSWORD", user.getId(), 1, "ok", ip, null);
        return R.success("密码已更新");
    }

    private void audit(String action, String userId, int success, String message, String ip, String ua) {
        AuditLogEntity log = new AuditLogEntity();
        log.setId(UUID.randomUUID().toString());
        log.setUserId(userId);
        log.setAction(action);
        log.setSuccess(success);
        log.setMessage(message);
        log.setIp(ip);
        log.setUa(ua);
        log.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(log);
    }

    private RefreshTokenEntity buildRevoked(String id) {
        RefreshTokenEntity e = new RefreshTokenEntity();
        e.setId(id);
        e.setRevoked(1);
        return e;
    }
    private String getVal(String key) {
        return kvStore.get(key);
    }
    private void setVal(String key, String val, long ttlSeconds) {
        kvStore.set(key, val, ttlSeconds);
    }
    private Long incVal(String key) {
        return kvStore.increment(key);
    }
    private void expireVal(String key, Duration duration) {
        kvStore.expire(key, duration);
    }
    private void delVal(String key) {
        kvStore.delete(key);
    }
}
