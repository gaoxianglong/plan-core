package com.gxl.plancore.task.application.dto;

import java.util.List;

/**
 * 任务统计结果 DTO
 * 包含图表数据和任务列表
 */
public class TaskStatsResult {

    /** 查询维度 WEEK / MONTH */
    private final String dimension;
    /** 时间区间起始日期 */
    private final String startDate;
    /** 时间区间结束日期 */
    private final String endDate;
    /** 已完成任务总数 */
    private final int totalCompleted;
    /** 任务总数 */
    private final int totalTasks;
    /** 总完成率（百分比） */
    private final double totalCompletionRate;
    /** 图表数据列表 */
    private final List<ChartDataItem> chartData;
    /** 已完成任务列表 */
    private final List<TaskDTO> completedTasks;
    /** 未完成任务列表 */
    private final List<TaskDTO> incompleteTasks;

    public TaskStatsResult(String dimension, String startDate, String endDate,
                           int totalCompleted, int totalTasks, double totalCompletionRate,
                           List<ChartDataItem> chartData,
                           List<TaskDTO> completedTasks, List<TaskDTO> incompleteTasks) {
        this.dimension = dimension;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCompleted = totalCompleted;
        this.totalTasks = totalTasks;
        this.totalCompletionRate = totalCompletionRate;
        this.chartData = chartData;
        this.completedTasks = completedTasks;
        this.incompleteTasks = incompleteTasks;
    }

    public String getDimension() {
        return dimension;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getTotalCompleted() {
        return totalCompleted;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public double getTotalCompletionRate() {
        return totalCompletionRate;
    }

    public List<ChartDataItem> getChartData() {
        return chartData;
    }

    public List<TaskDTO> getCompletedTasks() {
        return completedTasks;
    }

    public List<TaskDTO> getIncompleteTasks() {
        return incompleteTasks;
    }
}
