package com.gxl.plancore.user.interfaces.dto;

/**
 * 查询用户信息响应 DTO
 */
public class UserProfileResponse {

    private String userId;
    private String nickname;
    private String avatar;
    private String ipLocation;
    private int consecutiveDays;
    private String lastCheckInDate;
    private int nicknameModifyCount;
    private String nicknameNextModifyAt;

    public UserProfileResponse() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getIpLocation() {
        return ipLocation;
    }

    public void setIpLocation(String ipLocation) {
        this.ipLocation = ipLocation;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(int consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public String getLastCheckInDate() {
        return lastCheckInDate;
    }

    public void setLastCheckInDate(String lastCheckInDate) {
        this.lastCheckInDate = lastCheckInDate;
    }

    public int getNicknameModifyCount() {
        return nicknameModifyCount;
    }

    public void setNicknameModifyCount(int nicknameModifyCount) {
        this.nicknameModifyCount = nicknameModifyCount;
    }

    public String getNicknameNextModifyAt() {
        return nicknameNextModifyAt;
    }

    public void setNicknameNextModifyAt(String nicknameNextModifyAt) {
        this.nicknameNextModifyAt = nicknameNextModifyAt;
    }
}
