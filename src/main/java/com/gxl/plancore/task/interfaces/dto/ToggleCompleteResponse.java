package com.gxl.plancore.task.interfaces.dto;

/**
 * 完成/反完成任务响应 DTO
 */
public class ToggleCompleteResponse {

    private String id;
    private String status;
    private String completedAt;

    public ToggleCompleteResponse() {
    }

    public ToggleCompleteResponse(String id, String status, String completedAt) {
        this.id = id;
        this.status = status;
        this.completedAt = completedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
}
