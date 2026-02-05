package com.gxl.plancore.common.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 服务
 * 负责生成和验证 JWT Token
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final int accessTokenExpirationHours;
    private final int refreshTokenExpirationDays;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-hours}") int accessTokenExpirationHours,
            @Value("${app.jwt.refresh-token-expiration-days}") int refreshTokenExpirationDays) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationHours = accessTokenExpirationHours;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    /**
     * 生成 access_token
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param deviceId  设备ID
     * @return access_token
     */
    public String generateAccessToken(String userId, String sessionId, String deviceId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessTokenExpirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(userId)
                .claim("sid", sessionId)
                .claim("did", deviceId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成 refresh_token
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param deviceId  设备ID
     * @return refresh_token
     */
    public String generateRefreshToken(String userId, String sessionId, String deviceId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(userId)
                .claim("sid", sessionId)
                .claim("did", deviceId)
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 获取 access_token 过期时间
     *
     * @return 过期时间
     */
    public Instant getAccessTokenExpiry() {
        return Instant.now().plus(accessTokenExpirationHours, ChronoUnit.HOURS);
    }

    /**
     * 获取 refresh_token 过期时间
     *
     * @return 过期时间
     */
    public Instant getRefreshTokenExpiry() {
        return Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS);
    }

    /**
     * 解析并验证 Token
     *
     * @param token JWT Token
     * @return Claims
     * @throws ExpiredJwtException Token 过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户ID
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
        return parseToken(token).get("sid", String.class);
    }

    /**
     * 从 Token 中获取设备ID
     *
     * @param token JWT Token
     * @return 设备ID
     */
    public String getDeviceIdFromToken(String token) {
        return parseToken(token).get("did", String.class);
    }

    /**
     * 验证 Token 是否有效（未过期且签名正确）
     *
     * @param token JWT Token
     * @return true=有效，false=无效
     */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断 Token 是否已过期
     *
     * @param token JWT Token
     * @return true=已过期，false=未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
