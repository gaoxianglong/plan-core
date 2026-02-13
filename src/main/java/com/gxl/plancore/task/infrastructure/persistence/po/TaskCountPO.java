package com.gxl.plancore.task.infrastructure.persistence.po;

import java.time.LocalDate;

/**
 * 任务计数持久化对象
 * 用于 COUNT + GROUP BY 聚合查询结果
 */
public class TaskCountPO {

    private LocalDate date;
    private String priority;
    private String status;
    private int taskCount;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }
}
