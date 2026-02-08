package com.gxl.plancore.task.application.dto;

import java.util.List;
import java.util.Map;

/**
 * 任务列表查询结果
 */
public class TaskListResult {

    private final String date;
    private final Map<String, Boolean> hasUncheckedTasks;
    private final Map<String, List<TaskWithSubTasksDTO>> tasks;

    /**
     * 构造任务列表查询结果
     */
    public TaskListResult(String date, Map<String, Boolean> hasUncheckedTasks,
                          Map<String, List<TaskWithSubTasksDTO>> tasks) {
        this.date = date;
        this.hasUncheckedTasks = hasUncheckedTasks;
        this.tasks = tasks;
    }

    /**
     * 获取查询日期
     */
    public String getDate() {
        return date;
    }

    /**
     * 获取各象限是否有未完成任务
     */
    public Map<String, Boolean> getHasUncheckedTasks() {
        return hasUncheckedTasks;
    }

    /**
     * 获取按象限分组的任务列表
     */
    public Map<String, List<TaskWithSubTasksDTO>> getTasks() {
        return tasks;
    }
}
