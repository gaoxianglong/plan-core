package com.gxl.plancore.user.application.dto;

/**
 * 刷新 Token 结果 DTO
 * 应用层返回给接口层
 */
public class RefreshResult {

    private final String accessToken;
    private final String refreshToken;
    private final int expiresIn;

    public RefreshResult(String accessToken, String refreshToken, int expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }
}
