package com.gxl.plancore.task.interfaces.dto;

/**
 * 完成/反完成任务响应
 */
public class ToggleCompleteResponse {

    private String id;
    private String status;
    private String completedAt;

    /**
     * 构造完成/反完成响应
     */
    public ToggleCompleteResponse(String id, String status, String completedAt) {
        this.id = id;
        this.status = status;
        this.completedAt = completedAt;
    }

    /**
     * 获取任务ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置任务ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取任务状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置任务状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取完成时间
     */
    public String getCompletedAt() {
        return completedAt;
    }

    /**
     * 设置完成时间
     */
    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
}
