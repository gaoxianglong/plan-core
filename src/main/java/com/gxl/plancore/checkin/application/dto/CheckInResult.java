package com.gxl.plancore.checkin.application.dto;

import java.time.Instant;

/**
 * 打卡结果 DTO
 */
public class CheckInResult {

    private final String date;
    private final Instant checkedAt;
    private final int consecutiveDays;

    public CheckInResult(String date, Instant checkedAt, int consecutiveDays) {
        this.date = date;
        this.checkedAt = checkedAt;
        this.consecutiveDays = consecutiveDays;
    }

    public String getDate() {
        return date;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }
}
