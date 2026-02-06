package com.gxl.plancore.user.interfaces.dto;

/**
 * 更新用户信息响应 DTO
 */
public class UpdateProfileResponse {

    private String nickname;
    private String avatar;
    private String updatedAt;

    public UpdateProfileResponse() {
    }

    public UpdateProfileResponse(String nickname, String avatar, String updatedAt) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.updatedAt = updatedAt;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
