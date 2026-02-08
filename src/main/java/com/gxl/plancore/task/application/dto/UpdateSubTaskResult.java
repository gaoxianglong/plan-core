package com.gxl.plancore.task.application.dto;

import java.time.Instant;

/**
 * 更新子任务结果
 */
public class UpdateSubTaskResult {

    private final String subTaskId;
    private final String title;
    private final Instant updatedAt;

    /**
     * 构造更新子任务结果
     */
    public UpdateSubTaskResult(String subTaskId, String title, Instant updatedAt) {
        this.subTaskId = subTaskId;
        this.title = title;
        this.updatedAt = updatedAt;
    }

    /**
     * 获取子任务ID
     */
    public String getSubTaskId() {
        return subTaskId;
    }

    /**
     * 获取子任务标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取更新时间
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
