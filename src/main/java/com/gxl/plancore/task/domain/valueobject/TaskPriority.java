package com.gxl.plancore.task.domain.valueobject;

/**
 * 任务优先级枚举
 * P0-P3 对应四象限
 */
public enum TaskPriority {

    P0("紧急且重要"),
    P1("重要不紧急"),
    P2("紧急不重要"),
    P3("不重要不紧急");

    private final String label;

    TaskPriority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
