package com.gxl.plancore.user.interfaces.dto;

/**
 * 用户信息响应 DTO
 */
public class UserInfoResponse {
    
    private String nickname;
    private String avatar;
    private String ipLocation;
    
    public UserInfoResponse(String nickname, String avatar, String ipLocation) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.ipLocation = ipLocation;
    }
    
    // Getters
    public String getNickname() {
        return nickname;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public String getIpLocation() {
        return ipLocation;
    }
}
