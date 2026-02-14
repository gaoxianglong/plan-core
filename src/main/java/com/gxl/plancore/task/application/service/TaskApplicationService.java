package com.gxl.plancore.task.application.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.task.application.command.CreateTaskCommand;
import com.gxl.plancore.task.application.command.UpdateTaskCommand;
import com.gxl.plancore.task.application.dto.ChartDataItem;
import com.gxl.plancore.task.application.dto.CreateTaskResult;
import com.gxl.plancore.task.application.dto.DeleteTaskResult;
import com.gxl.plancore.task.application.dto.PerDateTaskData;
import com.gxl.plancore.task.application.dto.TaskDTO;
import com.gxl.plancore.task.application.dto.TaskListResult;
import com.gxl.plancore.task.application.dto.TaskStatsResult;
import com.gxl.plancore.task.application.dto.ToggleCompleteResult;
import com.gxl.plancore.task.application.dto.UpdateTaskResult;
import com.gxl.plancore.task.domain.entity.Task;
import com.gxl.plancore.task.domain.repository.TaskRepository;
import com.gxl.plancore.task.domain.valueobject.TaskDateCount;
import com.gxl.plancore.task.domain.valueobject.TaskPriority;
import com.gxl.plancore.task.domain.valueobject.TaskStatus;

/**
 * 任务应用服务
 * 编排任务相关业务流程
 */
@Service
public class TaskApplicationService {

    private static final Logger log = LoggerFactory.getLogger(TaskApplicationService.class);

    /** 任务标题最大长度 */
    private static final int TITLE_MAX_LENGTH = 100;

    /** 日期范围限制（天） */
    private static final int DATE_RANGE_DAYS = 365;

    /** 单日任务数上限 */
    private static final int DAILY_TASK_LIMIT = 50;

    private final TaskRepository taskRepository;

    public TaskApplicationService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * 创建任务
     *
     * @param command 创建任务命令
     * @return 创建结果
     */
    @Transactional
    public CreateTaskResult createTask(CreateTaskCommand command) {
        log.info("创建任务: userId={}, title={}, priority={}, date={}",
                command.getUserId(), command.getTitle(), command.getPriority(), command.getDate());

        // 1. 校验标题
        validateTitle(command.getTitle());

        // 2. 校验优先级
        TaskPriority priority = validatePriority(command.getPriority());

        // 3. 校验日期
        LocalDate date = validateDate(command.getDate());

        // 4. 校验单日任务数上限
        int count = taskRepository.countByUserIdAndDate(command.getUserId(), date.toString());
        if (count >= DAILY_TASK_LIMIT) {
            log.warn("创建任务失败: 单日任务数超出上限, userId={}, date={}, count={}",
                    command.getUserId(), date, count);
            throw new BusinessException(ErrorCode.TASK_DAILY_LIMIT);
        }

        // 5. 创建任务实体
        Task task = Task.create(command.getUserId(), command.getTitle().trim(), priority, date);

        // 6. 持久化
        taskRepository.save(task);

        log.info("创建任务成功: taskId={}, userId={}", task.getTaskId(), task.getUserId());

        // 7. 构建结果
        return new CreateTaskResult(
                task.getTaskId(),
                task.getTitle(),
                task.getPriority().name(),
                task.getStatus().name(),
                task.getDate(),
                task.getCreatedAt()
        );
    }

    /**
     * 查询指定日期的任务列表（按优先级分组）
     *
     * @param userId        用户ID
     * @param dateStr       日期字符串 YYYY-MM-DD
     * @param showCompleted 是否显示已完成任务
     * @return 按优先级分组的任务列表
     */
    public TaskListResult queryTasksByDate(String userId, String dateStr, boolean showCompleted) {
        log.info("查询任务列表: userId={}, date={}, showCompleted={}", userId, dateStr, showCompleted);

        // 1. 校验日期格式
        LocalDate date = validateDate(dateStr);

        // 2. 查询该日期下的所有任务
        List<Task> allTasks = taskRepository.findByUserIdAndDate(userId, date.toString());

        // 3. 初始化四象限分组（使用 LinkedHashMap 保持 P0-P3 顺序）
        Map<String, List<TaskDTO>> tasksMap = new LinkedHashMap<>();
        tasksMap.put("P0", new ArrayList<>());
        tasksMap.put("P1", new ArrayList<>());
        tasksMap.put("P2", new ArrayList<>());
        tasksMap.put("P3", new ArrayList<>());

        Map<String, Boolean> hasUncheckedTasks = new LinkedHashMap<>();
        hasUncheckedTasks.put("P0", false);
        hasUncheckedTasks.put("P1", false);
        hasUncheckedTasks.put("P2", false);
        hasUncheckedTasks.put("P3", false);

        // 4. 遍历任务，按优先级分组
        for (Task task : allTasks) {
            String priorityKey = task.getPriority().name();

            // 标记是否有未完成任务
            if (task.getStatus() == TaskStatus.INCOMPLETE) {
                hasUncheckedTasks.put(priorityKey, true);
            }

            // 根据 showCompleted 过滤已完成任务
            if (!showCompleted && task.getStatus() == TaskStatus.COMPLETED) {
                continue;
            }

            TaskDTO dto = new TaskDTO(
                    task.getTaskId(),
                    task.getTitle(),
                    task.getPriority().name(),
                    task.getStatus().name(),
                    task.getDate(),
                    task.getCreatedAt(),
                    task.getCompletedAt()
            );
            tasksMap.get(priorityKey).add(dto);
        }

        log.info("查询任务列表完成: userId={}, date={}, totalTasks={}", userId, dateStr, allTasks.size());
        return new TaskListResult(dateStr, hasUncheckedTasks, tasksMap);
    }

    /**
     * 查询多个日期的任务列表（按优先级分组）
     *
     * @param userId        用户ID
     * @param dateStrList   日期字符串列表 YYYY-MM-DD
     * @param showCompleted 是否显示已完成任务
     * @return 按日期分组的任务列表
     */
    public TaskListResult queryTasksByDates(String userId, List<String> dateStrList, boolean showCompleted) {
        if (dateStrList == null || dateStrList.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "dates 参数不能为空");
        }

        log.info("查询任务列表（多日期）: userId={}, dates={}, showCompleted={}", userId, dateStrList, showCompleted);

        // 1. 校验并解析日期，去重且保持顺序
        List<LocalDate> dates = new ArrayList<>();
        for (String dateStr : dateStrList) {
            LocalDate d = validateDate(dateStr.trim());
            if (!dates.contains(d)) {
                dates.add(d);
            }
        }

        if (dates.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "dates 参数不能为空");
        }

        // 2. 确定范围，一次查询
        LocalDate minDate = dates.get(0);
        LocalDate maxDate = dates.get(0);
        for (LocalDate d : dates) {
            if (d.isBefore(minDate)) {
                minDate = d;
            }
            if (d.isAfter(maxDate)) {
                maxDate = d;
            }
        }

        Set<String> requestedDateSet = new HashSet<>();
        for (LocalDate d : dates) {
            requestedDateSet.add(d.toString());
        }

        List<Task> allTasks = taskRepository.findByUserIdAndDateRange(
                userId, minDate.toString(), maxDate.toString());

        // 3. 按日期分组
        Map<String, List<Task>> tasksByDate = new LinkedHashMap<>();
        for (String dateStr : requestedDateSet) {
            tasksByDate.put(dateStr, new ArrayList<>());
        }
        for (Task task : allTasks) {
            String dateStr = task.getDate().toString();
            if (requestedDateSet.contains(dateStr)) {
                tasksByDate.get(dateStr).add(task);
            }
        }

        // 4. 为每个日期构建 PerDateTaskData（按请求顺序）
        Map<String, PerDateTaskData> dataByDate = new LinkedHashMap<>();
        for (LocalDate d : dates) {
            String dateKey = d.toString();
            List<Task> dayTasks = tasksByDate.get(dateKey);
            if (dayTasks == null) {
                dayTasks = new ArrayList<>();
            }

            Map<String, List<TaskDTO>> tasksMap = new LinkedHashMap<>();
            tasksMap.put("P0", new ArrayList<>());
            tasksMap.put("P1", new ArrayList<>());
            tasksMap.put("P2", new ArrayList<>());
            tasksMap.put("P3", new ArrayList<>());

            Map<String, Boolean> hasUncheckedTasks = new LinkedHashMap<>();
            hasUncheckedTasks.put("P0", false);
            hasUncheckedTasks.put("P1", false);
            hasUncheckedTasks.put("P2", false);
            hasUncheckedTasks.put("P3", false);

            for (Task task : dayTasks) {
                String priorityKey = task.getPriority().name();
                if (task.getStatus() == TaskStatus.INCOMPLETE) {
                    hasUncheckedTasks.put(priorityKey, true);
                }
                if (!showCompleted && task.getStatus() == TaskStatus.COMPLETED) {
                    continue;
                }
                TaskDTO dto = new TaskDTO(
                        task.getTaskId(),
                        task.getTitle(),
                        task.getPriority().name(),
                        task.getStatus().name(),
                        task.getDate(),
                        task.getCreatedAt(),
                        task.getCompletedAt()
                );
                tasksMap.get(priorityKey).add(dto);
            }

            dataByDate.put(dateKey, new PerDateTaskData(hasUncheckedTasks, tasksMap));
        }

        int totalTasks = allTasks.size();
        log.info("查询任务列表完成（多日期）: userId={}, dateCount={}, totalTasks={}", userId, dates.size(), totalTasks);
        return new TaskListResult(dataByDate);
    }

    /**
     * 更新任务
     *
     * @param command 更新任务命令
     * @return 更新结果
     */
    @Transactional
    public UpdateTaskResult updateTask(UpdateTaskCommand command) {
        log.info("更新任务: userId={}, taskId={}", command.getUserId(), command.getTaskId());

        // 1. 查找任务
        java.util.Optional<Task> taskOpt = taskRepository.findByTaskId(command.getTaskId());
        if (taskOpt.isEmpty()) {
            log.warn("更新任务失败: 任务不存在, taskId={}", command.getTaskId());
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        Task task = taskOpt.get();

        // 2. 校验任务归属
        if (!task.getUserId().equals(command.getUserId())) {
            log.warn("更新任务失败: 任务不属于当前用户, taskId={}, userId={}",
                    command.getTaskId(), command.getUserId());
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // 3. 校验并解析可选字段
        String newTitle = null;
        if (command.getTitle() != null && !command.getTitle().trim().isEmpty()) {
            validateTitle(command.getTitle());
            newTitle = command.getTitle().trim();
        }

        TaskPriority newPriority = null;
        if (command.getPriority() != null) {
            newPriority = validatePriority(command.getPriority());
        }

        LocalDate newDate = null;
        if (command.getDate() != null) {
            newDate = validateDate(command.getDate());
        }

        // 4. 更新领域对象
        task.updateInfo(newTitle, newPriority, newDate);

        // 5. 持久化
        taskRepository.update(task);

        log.info("更新任务成功: taskId={}", task.getTaskId());

        return new UpdateTaskResult(
                task.getTaskId(),
                task.getTitle(),
                task.getPriority().name(),
                task.getDate(),
                task.getUpdatedAt()
        );
    }

    /**
     * 删除任务（逻辑删除，幂等）
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 删除结果
     */
    @Transactional
    public DeleteTaskResult deleteTask(String userId, String taskId) {
        log.info("删除任务: userId={}, taskId={}", userId, taskId);

        // 1. 查找任务（不存在视为已删除，幂等返回成功）
        java.util.Optional<Task> taskOpt = taskRepository.findByTaskId(taskId);
        if (taskOpt.isEmpty()) {
            log.info("删除任务: 任务不存在或已删除, taskId={}", taskId);
            return new DeleteTaskResult(0);
        }

        Task task = taskOpt.get();

        // 2. 校验任务归属
        if (!task.getUserId().equals(userId)) {
            log.warn("删除任务失败: 任务不属于当前用户, taskId={}, userId={}", taskId, userId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // 3. 逻辑删除
        int deletedCount = taskRepository.softDelete(taskId);

        log.info("删除任务成功: taskId={}, deletedCount={}", taskId, deletedCount);
        return new DeleteTaskResult(deletedCount);
    }

    /**
     * 完成/反完成任务
     *
     * @param userId    用户ID
     * @param taskId    任务ID
     * @param completed true=完成，false=反完成
     * @return 切换结果
     */
    @Transactional
    public ToggleCompleteResult toggleComplete(String userId, String taskId, boolean completed) {
        log.info("完成/反完成任务: userId={}, taskId={}, completed={}", userId, taskId, completed);

        // 1. 查找任务
        java.util.Optional<Task> taskOpt = taskRepository.findByTaskId(taskId);
        if (taskOpt.isEmpty()) {
            log.warn("完成/反完成任务失败: 任务不存在, taskId={}", taskId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        Task task = taskOpt.get();

        // 2. 校验任务归属
        if (!task.getUserId().equals(userId)) {
            log.warn("完成/反完成任务失败: 任务不属于当前用户, taskId={}, userId={}", taskId, userId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // 3. 切换完成状态
        if (completed) {
            task.complete();
        } else {
            task.uncomplete();
        }

        // 4. 持久化
        taskRepository.update(task);

        log.info("完成/反完成任务成功: taskId={}, status={}", taskId, task.getStatus().name());

        return new ToggleCompleteResult(
                task.getTaskId(),
                task.getStatus().name(),
                task.getCompletedAt()
        );
    }

    /**
     * 查询任务统计视图（周/月维度）
     * 拆分为两次独立数据库查询以降低服务器压力：
     *   查询1：COUNT + GROUP BY 聚合统计（轻量，仅返回计数）
     *   查询2：仅查询当天（date 参数指定的那一天）的任务明细
     *
     * @param userId       用户ID
     * @param dateStr      基准日期字符串 YYYY-MM-DD（同时用于确定区间 + 查询当日明细）
     * @param dimension    维度：WEEK 或 MONTH
     * @param prioritiesStr 优先级筛选，逗号分隔（可选，null 或空表示全部）
     * @return 统计结果
     */
    public TaskStatsResult queryTaskStats(String userId, String dateStr, String dimension, String prioritiesStr) {
        log.info("查询任务统计: userId={}, date={}, dimension={}, priorities={}",
                userId, dateStr, dimension, prioritiesStr);

        // 1. 校验维度
        if (!"WEEK".equals(dimension) && !"MONTH".equals(dimension)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "维度参数必须为 WEEK 或 MONTH");
        }

        // 2. 校验日期
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "日期格式必须为 YYYY-MM-DD");
        }

        // 3. 解析优先级筛选
        Set<String> priorityFilter = parsePriorities(prioritiesStr);

        // 4. 确定时间区间
        LocalDate startDate;
        LocalDate endDate;
        if ("WEEK".equals(dimension)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            startDate = date.minusDays(dayOfWeek.getValue() - 1);
            endDate = startDate.plusDays(6);
        } else {
            startDate = date.withDayOfMonth(1);
            endDate = date.withDayOfMonth(date.lengthOfMonth());
        }

        // ========== 查询1：聚合统计（轻量级，只返回计数） ==========
        List<TaskDateCount> allCounts = taskRepository.countGroupedByDateRange(
                userId, startDate.toString(), endDate.toString());

        // 按优先级过滤计数
        List<TaskDateCount> filteredCounts = filterCountsByPriorities(allCounts, priorityFilter);

        // 构建图表数据
        List<ChartDataItem> chartData;
        if ("WEEK".equals(dimension)) {
            chartData = buildWeekChartDataFromCounts(startDate, filteredCounts);
        } else {
            chartData = buildMonthChartDataFromCounts(startDate, endDate, filteredCounts);
        }

        // 计算总完成数和总任务数
        int totalCompleted = 0;
        int totalTasks = 0;
        for (TaskDateCount tc : filteredCounts) {
            totalTasks += tc.getCount();
            if ("COMPLETED".equals(tc.getStatus())) {
                totalCompleted += tc.getCount();
            }
        }
        double totalCompletionRate = calculateRate(totalCompleted, totalTasks);

        // ========== 查询2：查询时间区间内的任务明细（周维度=一周，月维度=一月） ==========
        List<Task> rangeTasks = taskRepository.findByUserIdAndDateRange(
                userId, startDate.toString(), endDate.toString());
        List<Task> filteredRangeTasks = filterByPriorities(rangeTasks, priorityFilter);

        // 分离已完成和未完成任务列表
        List<TaskDTO> completedTasks = new ArrayList<>();
        List<TaskDTO> incompleteTasks = new ArrayList<>();

        for (Task task : filteredRangeTasks) {
            TaskDTO dto = new TaskDTO(
                    task.getTaskId(),
                    task.getTitle(),
                    task.getPriority().name(),
                    task.getStatus().name(),
                    task.getDate(),
                    task.getCreatedAt(),
                    task.getCompletedAt()
            );

            if (task.getStatus() == TaskStatus.COMPLETED) {
                completedTasks.add(dto);
            } else {
                incompleteTasks.add(dto);
            }
        }

        log.info("查询任务统计完成: userId={}, dimension={}, range=[{}, {}], totalTasks={}, totalCompleted={}, rangeTasks={}",
                userId, dimension, startDate, endDate, totalTasks, totalCompleted, filteredRangeTasks.size());

        return new TaskStatsResult(
                dimension,
                startDate.toString(),
                endDate.toString(),
                totalCompleted,
                totalTasks,
                totalCompletionRate,
                chartData,
                completedTasks,
                incompleteTasks
        );
    }

    /**
     * 解析优先级筛选参数
     *
     * @param prioritiesStr 逗号分隔的优先级字符串，如 "P0,P1"
     * @return 优先级集合，空集合表示不筛选（全部）
     */
    private Set<String> parsePriorities(String prioritiesStr) {
        Set<String> priorities = new HashSet<>();
        if (prioritiesStr == null || prioritiesStr.trim().isEmpty()) {
            return priorities;
        }
        String[] parts = prioritiesStr.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                try {
                    TaskPriority.valueOf(trimmed);
                    priorities.add(trimmed);
                } catch (IllegalArgumentException e) {
                    log.warn("忽略无效优先级筛选值: {}", trimmed);
                }
            }
        }
        return priorities;
    }

    /**
     * 按优先级过滤计数数据
     */
    private List<TaskDateCount> filterCountsByPriorities(List<TaskDateCount> counts, Set<String> priorityFilter) {
        if (priorityFilter.isEmpty()) {
            return counts;
        }
        List<TaskDateCount> result = new ArrayList<>();
        for (TaskDateCount tc : counts) {
            if (priorityFilter.contains(tc.getPriority())) {
                result.add(tc);
            }
        }
        return result;
    }

    /**
     * 按优先级过滤任务实体
     */
    private List<Task> filterByPriorities(List<Task> tasks, Set<String> priorityFilter) {
        if (priorityFilter.isEmpty()) {
            return tasks;
        }
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (priorityFilter.contains(task.getPriority().name())) {
                result.add(task);
            }
        }
        return result;
    }

    /**
     * 从聚合计数构建周维度图表数据（每天一条记录）
     */
    private List<ChartDataItem> buildWeekChartDataFromCounts(LocalDate startDate, List<TaskDateCount> counts) {
        List<ChartDataItem> chartData = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day = startDate.plusDays(i);
            int completed = 0;
            int incomplete = 0;

            for (TaskDateCount tc : counts) {
                if (tc.getDate().equals(day)) {
                    if ("COMPLETED".equals(tc.getStatus())) {
                        completed += tc.getCount();
                    } else {
                        incomplete += tc.getCount();
                    }
                }
            }

            int total = completed + incomplete;
            double rate = calculateRate(completed, total);
            chartData.add(new ChartDataItem(day.toString(), completed, incomplete, total, rate));
        }

        return chartData;
    }

    /**
     * 从聚合计数构建月维度图表数据（按周分组）
     * 第1周: 1-7日, 第2周: 8-14日, 第3周: 15-21日, 第4周: 22-28日, 第5周: 29日-月末
     */
    private List<ChartDataItem> buildMonthChartDataFromCounts(LocalDate startDate, LocalDate endDate,
                                                              List<TaskDateCount> counts) {
        List<ChartDataItem> chartData = new ArrayList<>();

        int lastDay = endDate.getDayOfMonth();
        int weekCount = lastDay > 28 ? 5 : 4;

        for (int week = 1; week <= weekCount; week++) {
            int weekStartDay = (week - 1) * 7 + 1;
            int weekEndDay;
            if (week == weekCount) {
                weekEndDay = lastDay;
            } else {
                weekEndDay = week * 7;
            }

            int completed = 0;
            int incomplete = 0;

            for (TaskDateCount tc : counts) {
                int taskDay = tc.getDate().getDayOfMonth();
                if (taskDay >= weekStartDay && taskDay <= weekEndDay) {
                    if ("COMPLETED".equals(tc.getStatus())) {
                        completed += tc.getCount();
                    } else {
                        incomplete += tc.getCount();
                    }
                }
            }

            int total = completed + incomplete;
            double rate = calculateRate(completed, total);
            String label = "第" + week + "周";
            chartData.add(new ChartDataItem(label, completed, incomplete, total, rate));
        }

        return chartData;
    }

    /**
     * 计算完成率（百分比，保留2位小数）
     */
    private double calculateRate(int completed, int total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(completed * 10000.0 / total) / 100.0;
    }

    /**
     * 校验任务标题
     * 标题不能为空，长度 1-100 字符
     */
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.TASK_TITLE_INVALID);
        }
        if (title.trim().length() > TITLE_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.TASK_TITLE_INVALID);
        }
    }

    /**
     * 校验优先级
     * 必须是 P0/P1/P2/P3
     */
    private TaskPriority validatePriority(String priority) {
        try {
            return TaskPriority.valueOf(priority);
        } catch (IllegalArgumentException e) {
            log.warn("创建任务失败: 优先级无效, priority={}", priority);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "优先级必须是 P0/P1/P2/P3");
        }
    }

    /**
     * 校验日期
     * 格式 YYYY-MM-DD，范围 今天-365天 ~ 今天+365天
     */
    private LocalDate validateDate(String dateStr) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            log.warn("创建任务失败: 日期格式无效, date={}", dateStr);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "日期格式必须为 YYYY-MM-DD");
        }

        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusDays(DATE_RANGE_DAYS);
        LocalDate maxDate = today.plusDays(DATE_RANGE_DAYS);

        if (date.isBefore(minDate) || date.isAfter(maxDate)) {
            log.warn("创建任务失败: 日期超出范围, date={}, range=[{}, {}]", date, minDate, maxDate);
            throw new BusinessException(ErrorCode.TASK_DATE_OUT_OF_RANGE);
        }

        return date;
    }
}
