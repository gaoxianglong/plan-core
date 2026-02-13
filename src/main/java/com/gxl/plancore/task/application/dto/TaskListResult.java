package com.gxl.plancore.task.application.dto;

import java.util.List;
import java.util.Map;

/**
 * 任务列表结果 DTO
 * 应用层返回给接口层，按优先级分组
 */
public class TaskListResult {

    private final String date;
    private final Map<String, Boolean> hasUncheckedTasks;
    private final Map<String, List<TaskDTO>> tasks;

    public TaskListResult(String date, Map<String, Boolean> hasUncheckedTasks,
                          Map<String, List<TaskDTO>> tasks) {
        this.date = date;
        this.hasUncheckedTasks = hasUncheckedTasks;
        this.tasks = tasks;
    }

    public String getDate() {
        return date;
    }

    public Map<String, Boolean> getHasUncheckedTasks() {
        return hasUncheckedTasks;
    }

    public Map<String, List<TaskDTO>> getTasks() {
        return tasks;
    }
}
