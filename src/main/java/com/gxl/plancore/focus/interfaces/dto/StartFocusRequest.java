package com.gxl.plancore.focus.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 开始专注请求 DTO
 */
public class StartFocusRequest {

    @NotNull(message = "专注时长不能为空")
    private Integer durationSeconds;

    @NotBlank(message = "专注类型不能为空")
    private String type;

    public StartFocusRequest() {
    }

    public StartFocusRequest(Integer durationSeconds, String type) {
        this.durationSeconds = durationSeconds;
        this.type = type;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
