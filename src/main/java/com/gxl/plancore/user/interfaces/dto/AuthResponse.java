package com.gxl.plancore.user.interfaces.dto;

/**
 * 认证响应 DTO
 */
public class AuthResponse {
    
    private String userId;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserInfoResponse userInfo;
    private EntitlementResponse entitlement;
    
    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final AuthResponse response = new AuthResponse();
        
        public Builder userId(String userId) {
            response.userId = userId;
            return this;
        }
        
        public Builder accessToken(String accessToken) {
            response.accessToken = accessToken;
            return this;
        }
        
        public Builder refreshToken(String refreshToken) {
            response.refreshToken = refreshToken;
            return this;
        }
        
        public Builder expiresIn(long expiresIn) {
            response.expiresIn = expiresIn;
            return this;
        }
        
        public Builder userInfo(UserInfoResponse userInfo) {
            response.userInfo = userInfo;
            return this;
        }
        
        public Builder entitlement(EntitlementResponse entitlement) {
            response.entitlement = entitlement;
            return this;
        }
        
        public AuthResponse build() {
            return response;
        }
    }
    
    // Getters
    public String getUserId() {
        return userId;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public UserInfoResponse getUserInfo() {
        return userInfo;
    }
    
    public EntitlementResponse getEntitlement() {
        return entitlement;
    }
}
