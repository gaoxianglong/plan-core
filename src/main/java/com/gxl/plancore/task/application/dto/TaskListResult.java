package com.gxl.plancore.task.application.dto;

import java.util.List;
import java.util.Map;

/**
 * 任务列表结果 DTO
 * 应用层返回给接口层，按优先级分组
 * 单日期：date、hasUncheckedTasks、tasks 有值
 * 多日期：dataByDate 有值
 */
public class TaskListResult {

    /** 单日期时的日期 */
    private final String date;
    /** 单日期时各象限是否有未完成任务 */
    private final Map<String, Boolean> hasUncheckedTasks;
    /** 单日期时按象限分组的任务 */
    private final Map<String, List<TaskDTO>> tasks;
    /** 多日期时，key=日期，value=当日数据 */
    private final Map<String, PerDateTaskData> dataByDate;

    /** 单日期构造 */
    public TaskListResult(String date, Map<String, Boolean> hasUncheckedTasks,
                          Map<String, List<TaskDTO>> tasks) {
        this.date = date;
        this.hasUncheckedTasks = hasUncheckedTasks;
        this.tasks = tasks;
        this.dataByDate = null;
    }

    /** 多日期构造 */
    public TaskListResult(Map<String, PerDateTaskData> dataByDate) {
        this.date = null;
        this.hasUncheckedTasks = null;
        this.tasks = null;
        this.dataByDate = dataByDate;
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

    public Map<String, PerDateTaskData> getDataByDate() {
        return dataByDate;
    }

    public boolean isMultiDate() {
        return dataByDate != null;
    }
}
