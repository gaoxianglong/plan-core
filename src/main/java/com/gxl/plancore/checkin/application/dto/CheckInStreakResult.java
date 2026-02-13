package com.gxl.plancore.checkin.application.dto;

/**
 * 打卡连续天数结果 DTO
 */
public class CheckInStreakResult {

    /** 当前连续打卡天数 */
    private final int consecutiveDays;
    /** 最近一次打卡日期（可能为 null） */
    private final String lastCheckInDate;

    public CheckInStreakResult(int consecutiveDays, String lastCheckInDate) {
        this.consecutiveDays = consecutiveDays;
        this.lastCheckInDate = lastCheckInDate;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public String getLastCheckInDate() {
        return lastCheckInDate;
    }
}
