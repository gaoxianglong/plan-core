package com.gxl.plancore.task.application.dto;

import java.time.Instant;

/**
 * 完成/反完成子任务结果
 */
public class ToggleSubTaskCompleteResult {

    private final String subTaskId;
    private final String status;
    private final Instant completedAt;
    private final String parentTaskId;
    private final String parentTaskStatus;

    /**
     * 构造完成/反完成子任务结果
     */
    public ToggleSubTaskCompleteResult(String subTaskId, String status, Instant completedAt,
                                        String parentTaskId, String parentTaskStatus) {
        this.subTaskId = subTaskId;
        this.status = status;
        this.completedAt = completedAt;
        this.parentTaskId = parentTaskId;
        this.parentTaskStatus = parentTaskStatus;
    }

    /**
     * 获取子任务ID
     */
    public String getSubTaskId() {
        return subTaskId;
    }

    /**
     * 获取子任务状态
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

    /**
     * 获取父任务ID
     */
    public String getParentTaskId() {
        return parentTaskId;
    }

    /**
     * 获取父任务状态
     */
    public String getParentTaskStatus() {
        return parentTaskStatus;
    }
}
