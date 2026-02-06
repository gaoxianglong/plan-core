package com.gxl.plancore.focus.infrastructure.persistence.po;

import java.time.Instant;

/**
 * 用户专注统计持久化对象
 * 对应数据库 user_focus_stats 表
 */
public class FocusStatsPO {

    private Long id;
    private String userId;
    private Long totalSeconds;
    private Integer totalHours;
    private Integer sessionCount;
    private Instant createdAt;
    private Instant updatedAt;

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

    public Long getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(Long totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public Integer getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Integer totalHours) {
        this.totalHours = totalHours;
    }

    public Integer getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(Integer sessionCount) {
        this.sessionCount = sessionCount;
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
}
