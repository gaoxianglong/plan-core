package com.gxl.plancore.task.domain.valueobject;

import java.time.LocalDate;

/**
 * 任务日期计数值对象
 * 表示某天、某优先级、某状态下的任务数量
 */
public class TaskDateCount {

    private final LocalDate date;
    private final String priority;
    private final String status;
    private final int count;

    public TaskDateCount(LocalDate date, String priority, String status, int count) {
        this.date = date;
        this.priority = priority;
        this.status = status;
        this.count = count;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public int getCount() {
        return count;
    }
}
