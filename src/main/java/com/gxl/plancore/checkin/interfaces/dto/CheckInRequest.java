package com.gxl.plancore.checkin.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 打卡请求 DTO
 */
public class CheckInRequest {

    @NotBlank(message = "打卡日期不能为空")
    private String date;

    public CheckInRequest() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
