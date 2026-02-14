package com.gxl.plancore.task.application.dto;

import java.util.List;
import java.util.Map;

/**
 * 单日任务数据 DTO
 * 用于多日期查询时，每个日期的 hasUncheckedTasks 与 tasks
 */
public class PerDateTaskData {

    private final Map<String, Boolean> hasUncheckedTasks;
    private final Map<String, List<TaskDTO>> tasks;

    public PerDateTaskData(Map<String, Boolean> hasUncheckedTasks, Map<String, List<TaskDTO>> tasks) {
        this.hasUncheckedTasks = hasUncheckedTasks;
        this.tasks = tasks;
    }

    public Map<String, Boolean> getHasUncheckedTasks() {
        return hasUncheckedTasks;
    }

    public Map<String, List<TaskDTO>> getTasks() {
        return tasks;
    }
}
