package com.gxl.plancore.user.application.dto;

import java.time.Instant;

/**
 * 登录结果 DTO
 * 包含用户信息和 Token
 */
public class LoginResult {

    private final String userId;
    private final String email;
    private final String nickname;
    private final String avatar;
    private final String accessToken;
    private final String refreshToken;
    private final Instant expiresAt;
    private final Instant refreshExpiresAt;

    public LoginResult(String userId, String email, String nickname, String avatar,
                       String accessToken, String refreshToken,
                       Instant expiresAt, Instant refreshExpiresAt) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.avatar = avatar;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRefreshExpiresAt() {
        return refreshExpiresAt;
    }
}
