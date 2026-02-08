package com.gxl.plancore.task.interfaces.dto;

/**
 * 删除任务响应
 */
public class DeleteTaskResponse {

    private int deletedCount;

    /**
     * 构造删除任务响应
     */
    public DeleteTaskResponse(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    /**
     * 获取删除数量
     */
    public int getDeletedCount() {
        return deletedCount;
    }

    /**
     * 设置删除数量
     */
    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }
}
