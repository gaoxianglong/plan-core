package com.gxl.plancore.user.infrastructure.persistence.po;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 用户持久化对象
 * 对应数据库 user 表
 */
public class UserPO {
    /**
     * 用户登录id
     */
    private Long id;
    private String userId;
    private String email;
    private String password;
    private String nickname;
    private String avatar;
    private String ipLocation;
    private Integer consecutiveDays;
    private LocalDate lastCheckInDate;
    private Integer nicknameModifyCount;
    private Instant nicknameFirstModifyAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
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
    
    public Integer getConsecutiveDays() {
        return consecutiveDays;
    }
    
    public void setConsecutiveDays(Integer consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }
    
    public LocalDate getLastCheckInDate() {
        return lastCheckInDate;
    }
    
    public void setLastCheckInDate(LocalDate lastCheckInDate) {
        this.lastCheckInDate = lastCheckInDate;
    }
    
    public Integer getNicknameModifyCount() {
        return nicknameModifyCount;
    }
    
    public void setNicknameModifyCount(Integer nicknameModifyCount) {
        this.nicknameModifyCount = nicknameModifyCount;
    }
    
    public Instant getNicknameFirstModifyAt() {
        return nicknameFirstModifyAt;
    }
    
    public void setNicknameFirstModifyAt(Instant nicknameFirstModifyAt) {
        this.nicknameFirstModifyAt = nicknameFirstModifyAt;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Instant getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
