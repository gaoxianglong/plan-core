package com.gxl.plancore.task.application.command;

import java.time.LocalDate;
import java.util.Map;

/**
 * 创建任务命令
 */
public class CreateTaskCommand {

    private final String userId;
    private final String requestId;
    private final String title;
    private final String priority;
    private final LocalDate date;
    private final String repeatType;
    private final Map<String, Object> repeatConfig;
    private final LocalDate repeatEndDate;

    /**
     * 构造创建任务命令
     */
    public CreateTaskCommand(String userId, String requestId, String title, String priority,
                             LocalDate date, String repeatType, Map<String, Object> repeatConfig,
                             LocalDate repeatEndDate) {
        this.userId = userId;
        this.requestId = requestId;
        this.title = title;
        this.priority = priority;
        this.date = date;
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.repeatEndDate = repeatEndDate;
    }

    /**
     * 获取用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取幂等请求ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 获取任务标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取任务优先级
     */
    public String getPriority() {
        return priority;
    }

    /**
     * 获取任务日期
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * 获取重复类型
     */
    public String getRepeatType() {
        return repeatType;
    }

    /**
     * 获取重复配置
     */
    public Map<String, Object> getRepeatConfig() {
        return repeatConfig;
    }

    /**
     * 获取重复结束日期
     */
    public LocalDate getRepeatEndDate() {
        return repeatEndDate;
    }
}
