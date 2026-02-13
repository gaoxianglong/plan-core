package com.gxl.plancore.task.interfaces.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 完成/反完成任务请求 DTO
 */
public class ToggleCompleteRequest {

    @NotNull(message = "completed 不能为空")
    private Boolean completed;

    public ToggleCompleteRequest() {
    }

    public ToggleCompleteRequest(Boolean completed) {
        this.completed = completed;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
