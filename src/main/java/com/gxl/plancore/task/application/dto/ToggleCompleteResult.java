package com.gxl.plancore.task.application.dto;

import java.time.Instant;

/**
 * 完成/反完成任务结果 DTO
 * 应用层返回给接口层
 */
public class ToggleCompleteResult {

    private final String taskId;
    private final String status;
    private final Instant completedAt;

    public ToggleCompleteResult(String taskId, String status, Instant completedAt) {
        this.taskId = taskId;
        this.status = status;
        this.completedAt = completedAt;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
