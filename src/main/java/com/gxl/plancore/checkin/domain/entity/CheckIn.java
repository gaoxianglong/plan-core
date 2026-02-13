package com.gxl.plancore.checkin.domain.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 打卡聚合根
 * 记录每日打卡信息及当次的连续天数快照
 */
public class CheckIn {

    /** 打卡UUID */
    private final String checkInId;
    /** 用户UUID */
    private final String userId;
    /** 打卡日期 */
    private final LocalDate date;
    /** 打卡时间 */
    private final Instant checkedAt;
    /** 打卡时的连续天数（快照） */
    private final int consecutiveDays;
    /** 创建时间 */
    private final Instant createdAt;

    /**
     * 创建打卡记录（工厂方法）
     *
     * @param userId          用户ID
     * @param date            打卡日期
     * @param consecutiveDays 本次打卡的连续天数
     * @return 打卡实体
     */
    public static CheckIn create(String userId, LocalDate date, int consecutiveDays) {
        Instant now = Instant.now();
        return new CheckIn(
                UUID.randomUUID().toString(),
                userId,
                date,
                now,
                consecutiveDays,
                now
        );
    }

    /**
     * 从持久化恢复
     */
    public static CheckIn reconstitute(
            String checkInId, String userId, LocalDate date,
            Instant checkedAt, int consecutiveDays, Instant createdAt) {
        return new CheckIn(checkInId, userId, date, checkedAt, consecutiveDays, createdAt);
    }

    private CheckIn(String checkInId, String userId, LocalDate date,
                    Instant checkedAt, int consecutiveDays, Instant createdAt) {
        this.checkInId = checkInId;
        this.userId = userId;
        this.date = date;
        this.checkedAt = checkedAt;
        this.consecutiveDays = consecutiveDays;
        this.createdAt = createdAt;
    }

    public String getCheckInId() {
        return checkInId;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
