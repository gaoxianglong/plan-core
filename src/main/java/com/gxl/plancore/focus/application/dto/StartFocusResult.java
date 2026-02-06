package com.gxl.plancore.focus.application.dto;

import java.time.Instant;

/**
 * 开始专注结果 DTO
 * 应用层返回给接口层
 */
public class StartFocusResult {

    private final String sessionId;
    private final int durationSeconds;
    private final String type;
    private final Instant startAt;
    private final Instant expectedEndAt;

    public StartFocusResult(String sessionId, int durationSeconds, String type,
                            Instant startAt, Instant expectedEndAt) {
        this.sessionId = sessionId;
        this.durationSeconds = durationSeconds;
        this.type = type;
        this.startAt = startAt;
        this.expectedEndAt = expectedEndAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getType() {
        return type;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getExpectedEndAt() {
        return expectedEndAt;
    }
}
