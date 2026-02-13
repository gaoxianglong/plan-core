package com.gxl.plancore.task.interfaces.dto;

import java.util.List;

/**
 * 任务统计视图响应 DTO
 */
public class TaskStatsResponse {

    /** 查询维度 WEEK / MONTH */
    private String dimension;
    /** 时间区间起始日期 */
    private String startDate;
    /** 时间区间结束日期 */
    private String endDate;
    /** 已完成任务总数 */
    private int totalCompleted;
    /** 任务总数 */
    private int totalTasks;
    /** 总完成率（百分比） */
    private double totalCompletionRate;
    /** 图表数据列表 */
    private List<ChartDataResponse> chartData;
    /** 已完成任务列表 */
    private List<TaskResponse> completedTasks;
    /** 未完成任务列表 */
    private List<TaskResponse> incompleteTasks;

    public TaskStatsResponse() {
    }

    public TaskStatsResponse(String dimension, String startDate, String endDate,
                             int totalCompleted, int totalTasks, double totalCompletionRate,
                             List<ChartDataResponse> chartData,
                             List<TaskResponse> completedTasks, List<TaskResponse> incompleteTasks) {
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

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getTotalCompleted() {
        return totalCompleted;
    }

    public void setTotalCompleted(int totalCompleted) {
        this.totalCompleted = totalCompleted;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public double getTotalCompletionRate() {
        return totalCompletionRate;
    }

    public void setTotalCompletionRate(double totalCompletionRate) {
        this.totalCompletionRate = totalCompletionRate;
    }

    public List<ChartDataResponse> getChartData() {
        return chartData;
    }

    public void setChartData(List<ChartDataResponse> chartData) {
        this.chartData = chartData;
    }

    public List<TaskResponse> getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(List<TaskResponse> completedTasks) {
        this.completedTasks = completedTasks;
    }

    public List<TaskResponse> getIncompleteTasks() {
        return incompleteTasks;
    }

    public void setIncompleteTasks(List<TaskResponse> incompleteTasks) {
        this.incompleteTasks = incompleteTasks;
    }
}
