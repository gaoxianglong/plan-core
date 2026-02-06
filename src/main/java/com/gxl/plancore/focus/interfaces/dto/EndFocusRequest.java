package com.gxl.plancore.focus.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 结束专注请求 DTO
 */
public class EndFocusRequest {

    @NotNull(message = "已完成秒数不能为空")
    private Integer elapsedSeconds;

    @NotBlank(message = "结束类型不能为空")
    private String endType;

    public EndFocusRequest() {
    }

    public EndFocusRequest(Integer elapsedSeconds, String endType) {
        this.elapsedSeconds = elapsedSeconds;
        this.endType = endType;
    }

    public Integer getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(Integer elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public String getEndType() {
        return endType;
    }

    public void setEndType(String endType) {
        this.endType = endType;
    }
}
