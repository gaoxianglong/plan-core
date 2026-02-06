package com.gxl.plancore.focus.application.dto;

/**
 * 结束专注结果 DTO
 * 应用层返回给接口层
 */
public class EndFocusResult {

    private final String sessionId;
    private final boolean counted;
    private final int countedSeconds;
    private final long totalFocusTime;

    public EndFocusResult(String sessionId, boolean counted, int countedSeconds, long totalFocusTime) {
        this.sessionId = sessionId;
        this.counted = counted;
        this.countedSeconds = countedSeconds;
        this.totalFocusTime = totalFocusTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isCounted() {
        return counted;
    }

    public int getCountedSeconds() {
        return countedSeconds;
    }

    public long getTotalFocusTime() {
        return totalFocusTime;
    }
}
