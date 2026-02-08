package com.gxl.plancore.task.interfaces.dto;

/**
 * 子任务响应
 */
public class SubTaskResponse {

    private String id;
    private String parentId;
    private String title;
    private String status;
    private String repeatType;
    private String completedAt;

    /**
     * 构造子任务响应
     */
    public SubTaskResponse(String id, String parentId, String title, String status,
                           String repeatType, String completedAt) {
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
    public String getCompletedAt() {
        return completedAt;
    }
}
