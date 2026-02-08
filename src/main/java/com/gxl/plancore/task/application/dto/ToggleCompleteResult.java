package com.gxl.plancore.task.application.dto;

import java.time.Instant;

/**
 * 完成/反完成任务结果
 */
public class ToggleCompleteResult {

    private final String taskId;
    private final String status;
    private final Instant completedAt;

    /**
     * 构造完成/反完成结果
     */
    public ToggleCompleteResult(String taskId, String status, Instant completedAt) {
        this.taskId = taskId;
        this.status = status;
        this.completedAt = completedAt;
    }

    /**
     * 获取任务ID
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 获取任务状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 获取完成时间
     */
    public Instant getCompletedAt() {
        return completedAt;
    }
}
