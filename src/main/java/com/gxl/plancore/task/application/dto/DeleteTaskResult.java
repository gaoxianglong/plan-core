package com.gxl.plancore.task.application.dto;

/**
 * 删除任务结果 DTO
 * 应用层返回给接口层
 */
public class DeleteTaskResult {

    private final int deletedCount;

    public DeleteTaskResult(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    public int getDeletedCount() {
        return deletedCount;
    }
}
