package com.gxl.plancore.user.application.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.user.application.dto.RefreshResult;
import com.gxl.plancore.user.domain.entity.DeviceSession;
import com.gxl.plancore.user.domain.repository.DeviceSessionRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 授权服务
 *
 * @author gxl
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final SecretKey secretKey;
    private final long accessTokenExpirationMinutes;
    private final long refreshTokenExpirationDays;
    private final DeviceSessionRepository deviceSessionRepository;

    public AuthService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes,
            @Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays,
            DeviceSessionRepository deviceSessionRepository) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.deviceSessionRepository = deviceSessionRepository;
    }

    /**
     * 生成 accessToken 和 refreshToken
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param deviceId  设备ID
     * @return 双 Token 结果
     */
    public TokenPair generateTokens(String userId, String sessionId, String deviceId) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);
        Instant refreshExpiresAt = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        String accessToken = buildToken(userId, sessionId, deviceId, "access", now, accessExpiresAt);
        String refreshToken = buildToken(userId, sessionId, deviceId, "refresh", now, refreshExpiresAt);

        log.info("生成双Token成功, userId={}, sessionId={}", userId, sessionId);
        return new TokenPair(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);
    }

    /**
     * 验证 accessToken 是否有效
     * 无效则抛出 UNAUTHORIZED 异常
     *
     * @param token accessToken
     * @return JWT Claims
     */
    public Claims validateAccessToken(String token) {
        try {
            return parseToken(token);
        } catch (JwtException e) {
            log.warn("accessToken验证失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * 验证 refreshToken 是否有效
     * 无效则抛出 UNAUTHORIZED 异常
     *
     * @param token refreshToken
     * @return JWT Claims
     */
    public Claims validateRefreshToken(String token) {
        // 1. 验证 refreshToken JWT 签名和有效期
        Claims claims;
        try {
            claims = parseToken(token);
        } catch (JwtException e) {
            log.warn("refreshToken验证失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 2. 从数据库查找会话，校验状态
        Optional<DeviceSession> sessionOpt = deviceSessionRepository.findByRefreshToken(token);
        if (sessionOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        DeviceSession session = sessionOpt.get();
        if (session.getStatus() != DeviceSession.Status.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return claims;
    }

    /**
     * 将 refreshToken 置为无效（退出登录）
     *
     * @param refreshToken 刷新令牌
     */
    public void invalidateRefreshToken(String refreshToken) {
        Optional<DeviceSession> sessionOpt = deviceSessionRepository.findByRefreshToken(refreshToken);
        if (sessionOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        DeviceSession session = sessionOpt.get();
        session.logout();
        deviceSessionRepository.update(session);
        log.info("refreshToken已失效, sessionId={}", session.getSessionId());
    }

    /**
     * accessToken 无效时，用 refreshToken 生成新的 accessToken
     * refreshToken 过期也返回未授权
     *
     * @param refreshToken 刷新令牌
     * @return 新的 accessToken 和过期时间
     */
    public RefreshResult refreshAccessToken(String refreshToken) {
        // 1. 验证 refreshToken JWT 签名和有效期
        Claims claims;
        try {
            claims = parseToken(refreshToken);
        } catch (JwtException e) {
            log.warn("refreshToken验证失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 2. 从数据库查找会话，校验状态
        Optional<DeviceSession> sessionOpt = deviceSessionRepository.findByRefreshToken(refreshToken);
        if (sessionOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        DeviceSession session = sessionOpt.get();
        if (session.getStatus() != DeviceSession.Status.ACTIVE) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 3. 生成新的 accessToken
        String userId = claims.getSubject();
        String sessionId = claims.get("sessionId", String.class);
        String deviceId = claims.get("deviceId", String.class);

        Instant now = Instant.now();
        Instant newExpiresAt = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);
        String newAccessToken = buildToken(userId, sessionId, deviceId, "access", now, newExpiresAt);

        // 4. 更新会话中的 accessToken
        session.refreshTokens(newAccessToken, session.getRefreshToken(), newExpiresAt, session.getRefreshExpiresAt());
        deviceSessionRepository.update(session);

        log.info("accessToken已刷新, sessionId={}", sessionId);
        int expiresIn = (int) ChronoUnit.SECONDS.between(now, newExpiresAt);
        return new RefreshResult(newAccessToken, refreshToken, expiresIn);
    }

    /**
     * 从 Token 中获取用户UserId
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 中获取会话ID
     *
     * @param token JWT Token
     * @return 会话ID
     */
    public String getSessionIdFromToken(String token) {
        return parseToken(token).get("sessionId", String.class);
    }

    /**
     * 从 Token 中获取设备ID
     *
     * @param token JWT Token
     * @return 设备ID
     */
    public String getDeviceIdFromToken(String token) {
        return parseToken(token).get("deviceId", String.class);
    }

    /**
     * 构建token
     * 
     * @param userId
     * @param sessionId
     * @param deviceId
     * @param type
     * @param issuedAt
     * @param expiresAt
     * @return
     */
    private String buildToken(String userId, String sessionId, String deviceId,
            String type, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .subject(userId)
                .claim("sessionId", sessionId)
                .claim("deviceId", deviceId)
                .claim("type", type)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析并验证 Token
     *
     * @param token JWT Token
     * @return Claims
     * @throws ExpiredJwtException Token 过期时抛出
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ==================== 内部结果类 ====================

    /**
     * 双 Token 结果
     */
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;
        private final Instant accessExpiresAt;
        private final Instant refreshExpiresAt;

        public TokenPair(String accessToken, String refreshToken,
                Instant accessExpiresAt, Instant refreshExpiresAt) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.accessExpiresAt = accessExpiresAt;
            this.refreshExpiresAt = refreshExpiresAt;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public Instant getAccessExpiresAt() {
            return accessExpiresAt;
        }

        public Instant getRefreshExpiresAt() {
            return refreshExpiresAt;
        }
    }
}
