package com.gxl.plancore.task.application.dto;

import java.time.Instant;

/**
 * 子任务 DTO
 */
public class SubTaskDTO {

    private final String id;
    private final String parentId;
    private final String title;
    private final String status;
    private final String repeatType;
    private final Instant completedAt;

    /**
     * 构造子任务 DTO
     */
    public SubTaskDTO(String id, String parentId, String title, String status,
                      String repeatType, Instant completedAt) {
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.status = status;
        this.repeatType = repeatType;
        this.completedAt = completedAt;
    }

    /**
     * 获取子任务ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取父任务ID
     */
    public String getParentId() {
        return parentId;
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
     * 获取完成时间
     */
    public Instant getCompletedAt() {
        return completedAt;
    }
}
