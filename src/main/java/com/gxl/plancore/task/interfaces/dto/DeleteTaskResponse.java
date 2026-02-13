package com.gxl.plancore.task.interfaces.dto;

/**
 * 删除任务响应 DTO
 */
public class DeleteTaskResponse {

    private int deletedCount;

    public DeleteTaskResponse() {
    }

    public DeleteTaskResponse(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }
}
