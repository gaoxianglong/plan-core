package com.gxl.plancore.user.application.dto;

import com.gxl.plancore.user.domain.entity.User;

import java.time.Instant;

/**
 * 用户 DTO
 */
public class UserDTO {
    
    private String userId;
    private String email;
    private String nickname;
    private String avatar;
    private String ipLocation;
    private int consecutiveDays;
    private Instant createdAt;
    
    public static UserDTO fromDomain(User user) {
        UserDTO dto = new UserDTO();
        dto.userId = user.getUserId().getValue();
        dto.email = user.getEmail().getValue();
        dto.nickname = user.getNickname().getValue();
        dto.avatar = user.getAvatar();
        dto.ipLocation = user.getIpLocation();
        dto.consecutiveDays = user.getConsecutiveDays();
        dto.createdAt = user.getCreatedAt();
        return dto;
    }
    
    // Getters
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
    
    public String getIpLocation() {
        return ipLocation;
    }
    
    public int getConsecutiveDays() {
        return consecutiveDays;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
}
