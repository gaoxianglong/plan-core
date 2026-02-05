package com.gxl.plancore.user.domain.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * 设备会话实体
 * 聚合根，管理用户的设备登录会话
 */
public class DeviceSession {

    /**
     * 会话状态枚举
     */
    public enum Status {
        ACTIVE,      // 活跃
        LOGGED_OUT   // 已退出
    }

    private final String sessionId;
    private final String userId;
    private final String deviceId;
    private final String deviceName;
    private final String platform;
    private final String osVersion;
    private final String appVersion;
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;
    private Instant refreshExpiresAt;
    private String lastLoginIp;
    private Instant lastLoginAt;
    private Instant lastActiveAt;
    private Status status;
    private Instant loggedOutAt;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * 创建新的设备会话（用于登录）
     */
    public static DeviceSession create(String userId, String deviceId, String deviceName,
                                       String platform, String osVersion, String appVersion,
                                       String accessToken, String refreshToken,
                                       Instant expiresAt, Instant refreshExpiresAt,
                                       String ipAddress) {
        Instant now = Instant.now();
        return new DeviceSession(
                UUID.randomUUID().toString(),
                userId,
                deviceId,
                deviceName,
                platform,
                osVersion,
                appVersion,
                accessToken,
                refreshToken,
                expiresAt,
                refreshExpiresAt,
                ipAddress,
                now,
                now,
                Status.ACTIVE,
                null,
                now,
                now
        );
    }

    /**
     * 从持久化恢复（用于 Repository）
     */
    public static DeviceSession reconstitute(String sessionId, String userId, String deviceId,
                                             String deviceName, String platform, String osVersion,
                                             String appVersion, String accessToken, String refreshToken,
                                             Instant expiresAt, Instant refreshExpiresAt,
                                             String lastLoginIp, Instant lastLoginAt, Instant lastActiveAt,
                                             Status status, Instant loggedOutAt,
                                             Instant createdAt, Instant updatedAt) {
        return new DeviceSession(
                sessionId, userId, deviceId, deviceName, platform, osVersion, appVersion,
                accessToken, refreshToken, expiresAt, refreshExpiresAt,
                lastLoginIp, lastLoginAt, lastActiveAt, status, loggedOutAt,
                createdAt, updatedAt
        );
    }

    private DeviceSession(String sessionId, String userId, String deviceId,
                          String deviceName, String platform, String osVersion,
                          String appVersion, String accessToken, String refreshToken,
                          Instant expiresAt, Instant refreshExpiresAt,
                          String lastLoginIp, Instant lastLoginAt, Instant lastActiveAt,
                          Status status, Instant loggedOutAt,
                          Instant createdAt, Instant updatedAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.platform = platform;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.refreshExpiresAt = refreshExpiresAt;
        this.lastLoginIp = lastLoginIp;
        this.lastLoginAt = lastLoginAt;
        this.lastActiveAt = lastActiveAt;
        this.status = status;
        this.loggedOutAt = loggedOutAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 刷新 Token
     */
    public void refreshTokens(String newAccessToken, String newRefreshToken,
                              Instant newExpiresAt, Instant newRefreshExpiresAt) {
        this.accessToken = newAccessToken;
        this.refreshToken = newRefreshToken;
        this.expiresAt = newExpiresAt;
        this.refreshExpiresAt = newRefreshExpiresAt;
        this.lastActiveAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 更新活跃时间
     */
    public void updateLastActiveAt() {
        this.lastActiveAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 退出登录
     */
    public void logout() {
        this.status = Status.LOGGED_OUT;
        this.loggedOutAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 判断 access_token 是否已过期
     */
    public boolean isAccessTokenExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * 判断 refresh_token 是否已过期
     */
    public boolean isRefreshTokenExpired() {
        return Instant.now().isAfter(refreshExpiresAt);
    }

    /**
     * 判断会话是否有效（活跃状态且 refresh_token 未过期）
     */
    public boolean isValid() {
        return status == Status.ACTIVE && !isRefreshTokenExpired();
    }

    // Getters
    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getPlatform() {
        return platform;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getAppVersion() {
        return appVersion;
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

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getLoggedOutAt() {
        return loggedOutAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
