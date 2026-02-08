package com.gxl.plancore.task.interfaces.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 完成/反完成任务请求
 */
public class ToggleCompleteRequest {

    @NotNull(message = "completed 不能为空")
    private Boolean completed;

    /**
     * 获取完成状态
     */
    public Boolean getCompleted() {
        return completed;
    }

    /**
     * 设置完成状态
     */
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
