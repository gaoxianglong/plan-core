package com.gxl.plancore.focus.domain.entity;

import java.time.Instant;

/**
 * 用户专注统计实体
 * 汇总表，避免每次聚合计算
 */
public class FocusStats {

    private final String userId;
    private long totalSeconds;
    private int totalHours;
    private int sessionCount;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * 首次创建统计记录
     */
    public static FocusStats create(String userId) {
        Instant now = Instant.now();
        return new FocusStats(userId, 0, 0, 0, now, now);
    }

    /**
     * 从持久化恢复
     */
    public static FocusStats reconstitute(String userId, long totalSeconds, int totalHours,
                                          int sessionCount, Instant createdAt, Instant updatedAt) {
        return new FocusStats(userId, totalSeconds, totalHours, sessionCount, createdAt, updatedAt);
    }

    private FocusStats(String userId, long totalSeconds, int totalHours,
                       int sessionCount, Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.totalSeconds = totalSeconds;
        this.totalHours = totalHours;
        this.sessionCount = sessionCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 增加专注时长（结束专注且 counted=true 时调用）
     *
     * @param countedSeconds 本次计入的秒数
     */
    public void addFocusTime(int countedSeconds) {
        this.totalSeconds = this.totalSeconds + countedSeconds;
        this.totalHours = (int) (this.totalSeconds / 3600);
        this.sessionCount = this.sessionCount + 1;
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public long getTotalSeconds() {
        return totalSeconds;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
