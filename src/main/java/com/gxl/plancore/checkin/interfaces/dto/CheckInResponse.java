package com.gxl.plancore.checkin.interfaces.dto;

/**
 * 打卡响应 DTO
 */
public class CheckInResponse {

    private String date;
    private String checkedAt;
    private int consecutiveDays;

    public CheckInResponse() {
    }

    public CheckInResponse(String date, String checkedAt, int consecutiveDays) {
        this.date = date;
        this.checkedAt = checkedAt;
        this.consecutiveDays = consecutiveDays;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(String checkedAt) {
        this.checkedAt = checkedAt;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(int consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }
}
