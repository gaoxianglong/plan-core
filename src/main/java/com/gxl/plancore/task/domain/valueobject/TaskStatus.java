package com.gxl.plancore.task.domain.valueobject;

/**
 * 任务状态枚举
 */
public enum TaskStatus {

    INCOMPLETE("未完成"),
    COMPLETED("已完成");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
