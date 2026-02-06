package com.gxl.plancore.focus.domain.entity;

import com.gxl.plancore.focus.domain.valueobject.EndType;
import com.gxl.plancore.focus.domain.valueobject.FocusDuration;
import com.gxl.plancore.focus.domain.valueobject.FocusType;
import com.gxl.plancore.focus.domain.valueobject.SessionId;
import com.gxl.plancore.focus.domain.valueobject.SessionStatus;

import java.time.Instant;

/**
 * 专注会话聚合根
 * 管理专注倒计时会话、计入规则
 */
public class FocusSession {

    // 标识
    private final SessionId sessionId;
    private final String userId;

    // 会话信息
    private final FocusDuration duration;
    private final FocusType type;
    private SessionStatus status;

    // 时间记录
    private final Instant startAt;
    private Instant endAt;
    private int elapsedSeconds;

    // 计入规则
    private EndType endType;
    private boolean counted;
    private int countedSeconds;

    // 审计
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * 开始专注（工厂方法）
     */
    public static FocusSession start(String userId, FocusDuration duration, FocusType type) {
        Instant now = Instant.now();
        return new FocusSession(
                SessionId.newId(),
                userId,
                duration,
                type,
                SessionStatus.RUNNING,
                now,
                null,
                0,
                null,
                false,
                0,
                now,
                now
        );
    }

    /**
     * 从持久化恢复
     */
    public static FocusSession reconstitute(
            SessionId sessionId, String userId,
            FocusDuration duration, FocusType type, SessionStatus status,
            Instant startAt, Instant endAt, int elapsedSeconds,
            EndType endType, boolean counted, int countedSeconds,
            Instant createdAt, Instant updatedAt) {
        return new FocusSession(
                sessionId, userId, duration, type, status,
                startAt, endAt, elapsedSeconds,
                endType, counted, countedSeconds,
                createdAt, updatedAt
        );
    }

    private FocusSession(SessionId sessionId, String userId,
                         FocusDuration duration, FocusType type, SessionStatus status,
                         Instant startAt, Instant endAt, int elapsedSeconds,
                         EndType endType, boolean counted, int countedSeconds,
                         Instant createdAt, Instant updatedAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.duration = duration;
        this.type = type;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
        this.elapsedSeconds = elapsedSeconds;
        this.endType = endType;
        this.counted = counted;
        this.countedSeconds = countedSeconds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 结束专注
     * 根据结束类型和已完成秒数，判断是否计入总时长
     *
     * 计入规则：
     * - 自然结束（NATURAL）：计入全部时长
     * - 手动结束（MANUAL）：已完成 >= 50% 则计入，否则不计入
     *
     * @param elapsedSeconds 已完成秒数
     * @param endType 结束类型
     * @throws IllegalStateException 会话不在 RUNNING 状态时抛出
     */
    public void end(int elapsedSeconds, EndType endType) {
        if (this.status != SessionStatus.RUNNING) {
            throw new IllegalStateException("会话不在进行中状态，无法结束");
        }

        this.elapsedSeconds = elapsedSeconds;
        this.endType = endType;
        this.endAt = Instant.now();
        this.status = SessionStatus.COMPLETED;

        // 计入规则
        if (endType == EndType.NATURAL) {
            // 自然结束：计入全部时长
            this.counted = true;
            this.countedSeconds = elapsedSeconds;
        } else {
            // 手动结束：达到 50% 才计入
            if (duration.isHalfway(elapsedSeconds)) {
                this.counted = true;
                this.countedSeconds = elapsedSeconds;
            } else {
                this.counted = false;
                this.countedSeconds = 0;
            }
        }

        this.updatedAt = Instant.now();
    }

    /**
     * 判断会话是否已结束
     */
    public boolean isEnded() {
        return this.status == SessionStatus.COMPLETED;
    }

    /**
     * 计算预计结束时间
     */
    public Instant getExpectedEndAt() {
        return startAt.plusSeconds(duration.getSeconds());
    }

    // Getters
    public SessionId getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public FocusDuration getDuration() {
        return duration;
    }

    public FocusType getType() {
        return type;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public EndType getEndType() {
        return endType;
    }

    public boolean isCounted() {
        return counted;
    }

    public int getCountedSeconds() {
        return countedSeconds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
