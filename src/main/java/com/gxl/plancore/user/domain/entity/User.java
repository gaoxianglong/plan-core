package com.gxl.plancore.user.domain.entity;

import com.gxl.plancore.user.domain.valueobject.Email;
import com.gxl.plancore.user.domain.valueobject.Nickname;
import com.gxl.plancore.user.domain.valueobject.Password;
import com.gxl.plancore.user.domain.valueobject.UserId;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 用户聚合根
 */
public class User {
    /**
     * 默认头像
     */
    private static final String DEFAULT_AVATAR = "avatar30.svg";

    // 标识
    private final UserId userId;
    private final Email email;
    private Password password;

    // 基本信息
    private Nickname nickname;
    private String avatar;
    private String ipLocation;

    // 打卡统计
    private int consecutiveDays;
    private LocalDate lastCheckInDate;

    // 昵称修改限制
    private int nicknameModifyCount;
    private Instant nicknameFirstModifyAt;

    // 审计字段
    private final Instant createdAt;
    private Instant updatedAt;

    private User(UserId userId, Email email, Password password, Nickname nickname, Instant createdAt) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.avatar = DEFAULT_AVATAR;
        this.consecutiveDays = 0;
        this.nicknameModifyCount = 0;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * 创建新用户（用于注册）
     */
    public static User create(Email email, Password password, Nickname nickname) {
        UserId userId = UserId.newId();
        Instant now = Instant.now();
        return new User(userId, email, password, nickname, now);
    }

    /**
     * 重建用户（用于从数据库加载）
     */
    public static User reconstruct(
            UserId userId,
            Email email,
            Password password,
            Nickname nickname,
            String avatar,
            String ipLocation,
            int consecutiveDays,
            LocalDate lastCheckInDate,
            int nicknameModifyCount,
            Instant nicknameFirstModifyAt,
            Instant createdAt,
            Instant updatedAt) {
        User user = new User(userId, email, password, nickname, createdAt);
        user.avatar = avatar;
        user.ipLocation = ipLocation;
        user.consecutiveDays = consecutiveDays;
        user.lastCheckInDate = lastCheckInDate;
        user.nicknameModifyCount = nicknameModifyCount;
        user.nicknameFirstModifyAt = nicknameFirstModifyAt;
        user.updatedAt = updatedAt;
        return user;
    }

    // Getters
    public UserId getUserId() {
        return userId;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public Nickname getNickname() {
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

    public LocalDate getLastCheckInDate() {
        return lastCheckInDate;
    }

    public int getNicknameModifyCount() {
        return nicknameModifyCount;
    }

    public Instant getNicknameFirstModifyAt() {
        return nicknameFirstModifyAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 修改密码
     * 
     * @param newPassword 新密码（已加密）
     */
    public void changePassword(Password newPassword) {
        this.password = newPassword;
        this.updatedAt = Instant.now();
    }

    /**
     * 修改昵称
     * 7天内最多修改2次
     * 
     * @param newNickname 新昵称值对象
     * @throws IllegalStateException 修改过于频繁时抛出
     */
    public void changeNickname(Nickname newNickname) {
        Instant now = Instant.now();

        // 判断是否在7天窗口内
        if (nicknameFirstModifyAt != null) {
            Instant windowEnd = nicknameFirstModifyAt.plusSeconds(7 * 24 * 60 * 60);
            if (now.isBefore(windowEnd)) {
                // 在7天窗口内，检查修改次数
                if (nicknameModifyCount >= 2) {
                    long remainSeconds = windowEnd.getEpochSecond() - now.getEpochSecond();
                    long remainDays = remainSeconds / (24 * 60 * 60) + 1;
                    throw new IllegalStateException(
                            String.format("昵称修改过于频繁，请 %d 天后再试", remainDays));
                }
                // 窗口内且次数未满，增加计数
                this.nicknameModifyCount = this.nicknameModifyCount + 1;
            } else {
                // 窗口已过，重置计数
                this.nicknameModifyCount = 1;
                this.nicknameFirstModifyAt = now;
            }
        } else {
            // 首次修改
            this.nicknameModifyCount = 1;
            this.nicknameFirstModifyAt = now;
        }

        this.nickname = newNickname;
        this.updatedAt = now;
    }

    /**
     * 修改头像
     * 
     * @param newAvatar 新头像标识
     */
    public void changeAvatar(String newAvatar) {
        if (newAvatar == null || newAvatar.trim().isEmpty()) {
            throw new IllegalArgumentException("头像标识不能为空");
        }
        this.avatar = newAvatar.trim();
        this.updatedAt = Instant.now();
    }
}
