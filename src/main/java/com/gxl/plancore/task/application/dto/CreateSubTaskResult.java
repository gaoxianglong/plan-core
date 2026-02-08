package com.gxl.plancore.task.application.dto;

import java.time.Instant;

/**
 * 创建子任务结果
 */
public class CreateSubTaskResult {

    private final String subTaskId;
    private final String parentTaskId;
    private final String title;
    private final String status;
    private final String repeatType;
    private final Instant createdAt;

    /**
     * 构造创建子任务结果
     */
    public CreateSubTaskResult(String subTaskId, String parentTaskId, String title,
                                String status, String repeatType, Instant createdAt) {
        this.subTaskId = subTaskId;
        this.parentTaskId = parentTaskId;
        this.title = title;
        this.status = status;
        this.repeatType = repeatType;
        this.createdAt = createdAt;
    }

    /**
     * 获取子任务ID
     */
    public String getSubTaskId() {
        return subTaskId;
    }

    /**
     * 获取父任务ID
     */
    public String getParentTaskId() {
        return parentTaskId;
    }

    /**
     * 获取子任务标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取子任务状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 获取重复类型
     */
    public String getRepeatType() {
        return repeatType;
    }

    /**
     * 获取创建时间
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
