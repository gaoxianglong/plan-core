package com.gxl.plancore.task.interfaces.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ApiResponse;
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
import com.gxl.plancore.task.application.service.TaskApplicationService;
import com.gxl.plancore.task.interfaces.dto.ChartDataResponse;
import com.gxl.plancore.task.interfaces.dto.CreateTaskRequest;
import com.gxl.plancore.task.interfaces.dto.CreateTaskResponse;
import com.gxl.plancore.task.interfaces.dto.DeleteTaskResponse;
import com.gxl.plancore.task.interfaces.dto.PerDateTaskListResponse;
import com.gxl.plancore.task.interfaces.dto.TaskListResponse;
import com.gxl.plancore.task.interfaces.dto.TaskResponse;
import com.gxl.plancore.task.interfaces.dto.TaskStatsResponse;
import com.gxl.plancore.task.interfaces.dto.ToggleCompleteRequest;
import com.gxl.plancore.task.interfaces.dto.ToggleCompleteResponse;
import com.gxl.plancore.task.interfaces.dto.UpdateTaskRequest;
import com.gxl.plancore.task.interfaces.dto.UpdateTaskResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskApplicationService taskApplicationService;

    public TaskController(TaskApplicationService taskApplicationService) {
        this.taskApplicationService = taskApplicationService;
    }

    /**
     * 查询任务列表（按日期）
     * 单日期：GET /api/v1/tasks?date=2026-02-10&showCompleted=true
     * 多日期：GET /api/v1/tasks?dates=2026-02-10,2026-02-11,2026-02-12&showCompleted=true
     */
    @GetMapping
    public ApiResponse<TaskListResponse> queryTasks(
            HttpServletRequest httpRequest,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "dates", required = false) String dates,
            @RequestParam(value = "showCompleted", defaultValue = "true") boolean showCompleted) {
        log.info("收到查询任务列表请求: date={}, dates={}, showCompleted={}", date, dates, showCompleted);

        if ((date == null || date.trim().isEmpty()) && (dates == null || dates.trim().isEmpty())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "date 与 dates 必填其一");
        }

        String userId = (String) httpRequest.getAttribute("userId");

        TaskListResult result;
        if (dates != null && !dates.trim().isEmpty()) {
            // 多日期
            List<String> dateList = new ArrayList<>();
            for (String d : dates.split(",")) {
                if (d != null && !d.trim().isEmpty()) {
                    dateList.add(d.trim());
                }
            }
            result = taskApplicationService.queryTasksByDates(userId, dateList, showCompleted);
        } else {
            // 单日期
            result = taskApplicationService.queryTasksByDate(userId, date.trim(), showCompleted);
        }

        TaskListResponse response = convertToResponse(result);
        return ApiResponse.success(response);
    }

    /**
     * 查询任务统计视图（周/月维度）
     * GET /api/v1/tasks/stats?dimension=WEEK&date=2026-02-12&priorities=P0,P1
     */
    @GetMapping("/stats")
    public ApiResponse<TaskStatsResponse> queryTaskStats(
            HttpServletRequest httpRequest,
            @RequestParam("dimension") String dimension,
            @RequestParam("date") String date,
            @RequestParam(value = "priorities", required = false) String priorities) {
        log.info("收到查询任务统计请求: dimension={}, date={}, priorities={}", dimension, date, priorities);

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");

        // 调用应用服务查询
        TaskStatsResult result = taskApplicationService.queryTaskStats(userId, date, dimension, priorities);

        // 转换为接口层响应
        TaskStatsResponse response = convertToStatsResponse(result);

        return ApiResponse.success(response);
    }

    /**
     * 创建任务
     * POST /api/v1/tasks
     */
    @PostMapping
    public ApiResponse<CreateTaskResponse> createTask(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreateTaskRequest request) {
        log.info("收到创建任务请求: title={}, priority={}, date={}",
                request.getTitle(), request.getPriority(), request.getDate());

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");

        // 构建命令并执行
        CreateTaskCommand command = new CreateTaskCommand(
                userId,
                request.getTitle(),
                request.getPriority(),
                request.getDate());
        CreateTaskResult result = taskApplicationService.createTask(command);

        // 构建响应
        CreateTaskResponse response = new CreateTaskResponse(
                result.getTaskId(),
                result.getTitle(),
                result.getPriority(),
                result.getStatus(),
                result.getDate().toString(),
                DateTimeFormatter.ISO_INSTANT.format(result.getCreatedAt()));

        return ApiResponse.success(response);
    }

    /**
     * 更新任务
     * PUT /api/v1/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    public ApiResponse<UpdateTaskResponse> updateTask(
            HttpServletRequest httpRequest,
            @PathVariable("taskId") String taskId,
            @RequestBody UpdateTaskRequest request) {
        log.info("收到更新任务请求: taskId={}, title={}, priority={}, date={}",
                taskId, request.getTitle(), request.getPriority(), request.getDate());

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");

        // 构建命令并执行
        UpdateTaskCommand command = new UpdateTaskCommand(
                userId,
                taskId,
                request.getTitle(),
                request.getPriority(),
                request.getDate());
        UpdateTaskResult result = taskApplicationService.updateTask(command);

        // 构建响应
        UpdateTaskResponse response = new UpdateTaskResponse(
                result.getTaskId(),
                result.getTitle(),
                result.getPriority(),
                result.getDate().toString(),
                DateTimeFormatter.ISO_INSTANT.format(result.getUpdatedAt()));

        return ApiResponse.success(response);
    }

    /**
     * 删除任务（逻辑删除）
     * DELETE /api/v1/tasks/{taskId}
     * 幂等：重复删除返回成功
     */
    @DeleteMapping("/{taskId}")
    public ApiResponse<DeleteTaskResponse> deleteTask(
            HttpServletRequest httpRequest,
            @PathVariable("taskId") String taskId) {
        log.info("收到删除任务请求: taskId={}", taskId);

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");

        // 执行删除
        DeleteTaskResult result = taskApplicationService.deleteTask(userId, taskId);

        // 构建响应
        DeleteTaskResponse response = new DeleteTaskResponse(result.getDeletedCount());

        return ApiResponse.success(response);
    }

    /**
     * 完成/反完成任务
     * POST /api/v1/tasks/{taskId}/toggle-complete
     */
    @PostMapping("/{taskId}/toggle-complete")
    public ApiResponse<ToggleCompleteResponse> toggleComplete(
            HttpServletRequest httpRequest,
            @PathVariable("taskId") String taskId,
            @Valid @RequestBody ToggleCompleteRequest request) {
        log.info("收到完成/反完成任务请求: taskId={}, completed={}", taskId, request.getCompleted());

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");

        // 执行完成/反完成
        ToggleCompleteResult result = taskApplicationService.toggleComplete(
                userId, taskId, request.getCompleted());

        // 构建响应
        String completedAtStr = null;
        if (result.getCompletedAt() != null) {
            completedAtStr = DateTimeFormatter.ISO_INSTANT.format(result.getCompletedAt());
        }

        ToggleCompleteResponse response = new ToggleCompleteResponse(
                result.getTaskId(),
                result.getStatus(),
                completedAtStr);

        return ApiResponse.success(response);
    }

    /**
     * 将应用层结果转换为接口层响应
     */
    private TaskListResponse convertToResponse(TaskListResult result) {
        if (result.isMultiDate()) {
            Map<String, PerDateTaskListResponse> dataByDate = new LinkedHashMap<>();
            for (Map.Entry<String, PerDateTaskData> entry : result.getDataByDate().entrySet()) {
                Map<String, List<TaskResponse>> tasksResponse = new LinkedHashMap<>();
                for (Map.Entry<String, List<TaskDTO>> taskEntry : entry.getValue().getTasks().entrySet()) {
                    List<TaskResponse> taskResponses = convertTaskDTOsToResponses(taskEntry.getValue());
                    tasksResponse.put(taskEntry.getKey(), taskResponses);
                }
                dataByDate.put(entry.getKey(), new PerDateTaskListResponse(
                        entry.getValue().getHasUncheckedTasks(), tasksResponse));
            }
            return new TaskListResponse(dataByDate);
        }

        Map<String, List<TaskResponse>> tasksResponse = new LinkedHashMap<>();
        for (Map.Entry<String, List<TaskDTO>> entry : result.getTasks().entrySet()) {
            List<TaskResponse> taskResponses = convertTaskDTOsToResponses(entry.getValue());
            tasksResponse.put(entry.getKey(), taskResponses);
        }
        return new TaskListResponse(result.getDate(), result.getHasUncheckedTasks(), tasksResponse);
    }

    /**
     * 将应用层统计结果转换为接口层响应
     */
    private TaskStatsResponse convertToStatsResponse(TaskStatsResult result) {
        // 转换图表数据
        List<ChartDataResponse> chartDataResponses = new ArrayList<>();
        for (ChartDataItem item : result.getChartData()) {
            chartDataResponses.add(new ChartDataResponse(
                    item.getLabel(),
                    item.getCompleted(),
                    item.getIncomplete(),
                    item.getTotal(),
                    item.getCompletionRate()
            ));
        }

        // 转换已完成任务列表
        List<TaskResponse> completedTaskResponses = convertTaskDTOsToResponses(result.getCompletedTasks());

        // 转换未完成任务列表
        List<TaskResponse> incompleteTaskResponses = convertTaskDTOsToResponses(result.getIncompleteTasks());

        return new TaskStatsResponse(
                result.getDimension(),
                result.getStartDate(),
                result.getEndDate(),
                result.getTotalCompleted(),
                result.getTotalTasks(),
                result.getTotalCompletionRate(),
                chartDataResponses,
                completedTaskResponses,
                incompleteTaskResponses
        );
    }

    /**
     * 将 TaskDTO 列表转换为 TaskResponse 列表
     */
    private List<TaskResponse> convertTaskDTOsToResponses(List<TaskDTO> dtos) {
        List<TaskResponse> responses = new ArrayList<>();
        for (TaskDTO dto : dtos) {
            String createdAtStr = DateTimeFormatter.ISO_INSTANT.format(dto.getCreatedAt());
            String completedAtStr = null;
            if (dto.getCompletedAt() != null) {
                completedAtStr = DateTimeFormatter.ISO_INSTANT.format(dto.getCompletedAt());
            }
            responses.add(new TaskResponse(
                    dto.getTaskId(),
                    dto.getTitle(),
                    dto.getPriority(),
                    dto.getStatus(),
                    dto.getDate().toString(),
                    createdAtStr,
                    completedAtStr
            ));
        }
        return responses;
    }
}
