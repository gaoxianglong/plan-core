package com.gxl.plancore.focus.interfaces.dto;

/**
 * 查询总专注时间响应 DTO
 */
public class TotalFocusTimeResponse {

    private long totalSeconds;
    private int totalHours;

    public TotalFocusTimeResponse() {
    }

    public TotalFocusTimeResponse(long totalSeconds, int totalHours) {
        this.totalSeconds = totalSeconds;
        this.totalHours = totalHours;
    }

    public long getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(long totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }
}
