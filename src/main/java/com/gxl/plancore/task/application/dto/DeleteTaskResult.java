package com.gxl.plancore.task.application.dto;

/**
 * 删除任务结果
 */
public class DeleteTaskResult {

    private final int deletedCount;

    /**
     * 构造删除任务结果
     */
    public DeleteTaskResult(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    /**
     * 获取删除数量
     */
    public int getDeletedCount() {
        return deletedCount;
    }
}
