package com.gxl.plancore.checkin.interfaces.dto;

/**
 * 打卡连续天数响应 DTO
 */
public class CheckInStreakResponse {

    private int consecutiveDays;
    private String lastCheckInDate;

    public CheckInStreakResponse() {
    }

    public CheckInStreakResponse(int consecutiveDays, String lastCheckInDate) {
        this.consecutiveDays = consecutiveDays;
        this.lastCheckInDate = lastCheckInDate;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(int consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public String getLastCheckInDate() {
        return lastCheckInDate;
    }

    public void setLastCheckInDate(String lastCheckInDate) {
        this.lastCheckInDate = lastCheckInDate;
    }
}
