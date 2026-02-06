package com.gxl.plancore.focus.interfaces.dto;

/**
 * 结束专注响应 DTO
 */
public class EndFocusResponse {

    private String sessionId;
    private boolean counted;
    private int countedSeconds;
    private long totalFocusTime;

    public EndFocusResponse() {
    }

    public EndFocusResponse(String sessionId, boolean counted, int countedSeconds, long totalFocusTime) {
        this.sessionId = sessionId;
        this.counted = counted;
        this.countedSeconds = countedSeconds;
        this.totalFocusTime = totalFocusTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isCounted() {
        return counted;
    }

    public void setCounted(boolean counted) {
        this.counted = counted;
    }

    public int getCountedSeconds() {
        return countedSeconds;
    }

    public void setCountedSeconds(int countedSeconds) {
        this.countedSeconds = countedSeconds;
    }

    public long getTotalFocusTime() {
        return totalFocusTime;
    }

    public void setTotalFocusTime(long totalFocusTime) {
        this.totalFocusTime = totalFocusTime;
    }
}
