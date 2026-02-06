package com.gxl.plancore.focus.domain.valueobject;

/**
 * 专注结束类型枚举
 */
public enum EndType {
    NATURAL("自然结束"),
    MANUAL("手动结束");

    private final String label;

    EndType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
