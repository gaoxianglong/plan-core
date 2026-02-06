package com.gxl.plancore.focus.domain.valueobject;

/**
 * 专注类型枚举
 */
public enum FocusType {
    WORK("工作"),
    STUDY("学习"),
    READING("阅读"),
    CODING("编程"),
    EXERCISE("运动"),
    MEDITATION("冥想"),
    OTHER("其它");

    private final String label;

    FocusType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
