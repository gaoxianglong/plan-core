package com.gxl.plancore.focus.domain.valueobject;

/**
 * 专注会话状态枚举
 */
public enum SessionStatus {
    RUNNING("进行中"),
    INTERRUPTED("已中断"),
    COMPLETED("已完成");

    private final String label;

    SessionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
