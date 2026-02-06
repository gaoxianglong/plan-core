package com.gxl.plancore.focus.interfaces.dto;

/**
 * 开始专注响应 DTO
 */
public class StartFocusResponse {

    private String sessionId;
    private int durationSeconds;
    private String type;
    private String startAt;
    private String expectedEndAt;

    public StartFocusResponse() {
    }

    public StartFocusResponse(String sessionId, int durationSeconds, String type,
                              String startAt, String expectedEndAt) {
        this.sessionId = sessionId;
        this.durationSeconds = durationSeconds;
        this.type = type;
        this.startAt = startAt;
        this.expectedEndAt = expectedEndAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getExpectedEndAt() {
        return expectedEndAt;
    }

    public void setExpectedEndAt(String expectedEndAt) {
        this.expectedEndAt = expectedEndAt;
    }
}
