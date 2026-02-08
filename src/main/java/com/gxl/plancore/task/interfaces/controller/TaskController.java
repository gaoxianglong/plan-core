package com.gxl.plancore.task.interfaces.controller;

import com.alibaba.fastjson2.JSON;
import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ApiResponse;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.common.service.JwtService;
import com.gxl.plancore.task.application.command.CreateTaskCommand;
import com.gxl.plancore.task.application.dto.CreateSubTaskResult;
import com.gxl.plancore.task.application.dto.CreateTaskResult;
import com.gxl.plancore.task.application.dto.MaterializeResult;
import com.gxl.plancore.task.application.dto.SubTaskDTO;
import com.gxl.plancore.task.application.dto.TaskListResult;
import com.gxl.plancore.task.application.dto.TaskWithSubTasksDTO;
import com.gxl.plancore.task.application.dto.ToggleCompleteResult;
import com.gxl.plancore.task.application.dto.UpdateTaskResult;
import com.gxl.plancore.task.application.dto.DeleteTaskResult;
import com.gxl.plancore.task.application.service.TaskApplicationService;
import com.gxl.plancore.task.interfaces.dto.CreateSubTaskRequest;
import com.gxl.plancore.task.interfaces.dto.CreateSubTaskResponse;
import com.gxl.plancore.task.interfaces.dto.CreateTaskRequest;
import com.gxl.plancore.task.interfaces.dto.CreateTaskResponse;
import com.gxl.plancore.task.interfaces.dto.MaterializeResponse;
import com.gxl.plancore.task.interfaces.dto.SubTaskResponse;
import com.gxl.plancore.task.interfaces.dto.TaskListResponse;
import com.gxl.plancore.task.interfaces.dto.TaskResponse;
import com.gxl.plancore.task.interfaces.dto.ToggleCompleteRequest;
import com.gxl.plancore.task.interfaces.dto.ToggleCompleteResponse;
import com.gxl.plancore.task.interfaces.dto.UpdateTaskRequest;
import com.gxl.plancore.task.interfaces.dto.UpdateTaskResponse;
import com.gxl.plancore.task.interfaces.dto.DeleteTaskRequest;
import com.gxl.plancore.task.interfaces.dto.DeleteTaskResponse;
import com.gxl.plancore.user.application.service.AuthApplicationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final TaskApplicationService taskApplicationService;
    private final AuthApplicationService authApplicationService;
    private final JwtService jwtService;

    /**
     * 构造任务控制器
     */
    public TaskController(TaskApplicationService taskApplicationService,
                          AuthApplicationService authApplicationService,
                          JwtService jwtService) {
        this.taskApplicationService = taskApplicationService;
        this.authApplicationService = authApplicationService;
        this.jwtService = jwtService;
    }

    /**
     * 查询任务列表（按日期）
     * GET /api/v1/tasks
     */
    @GetMapping
    public ApiResponse<TaskListResponse> queryTasks(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("date") String dateStr,
            @RequestParam(value = "showCompleted", required = false, defaultValue = "true") boolean showCompleted
    ) {
        log.info("收到查询任务列表请求: date={}, showCompleted={}", dateStr, showCompleted);

        String accessToken = extractAccessToken(authorization);
        authApplicationService.validateSession(accessToken);
        String userId = jwtService.getUserIdFromToken(accessToken);

        LocalDate date = parseDate(dateStr);
        TaskListResult result = taskApplicationService.queryTasksByDate(userId, date, showCompleted);
        TaskListResponse response = convertToResponse(result);

        return ApiResponse.success(response);
    }

    /**
     * 创建任务
     * POST /api/v1/tasks
     */
    @PostMapping
    public ApiResponse<CreateTaskResponse> createTask(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Request-Id") String requestId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        log.info("收到创建任务请求: title={}, priority={}, date={}, repeatType={}",
                request.getTitle(), request.getPriority(), request.getDate(), request.getRepeatType());

        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        String accessToken = extractAccessToken(authorization);
        authApplicationService.validateSession(accessToken);
        String userId = jwtService.getUserIdFromToken(accessToken);

        LocalDate date = parseDate(request.getDate());
        LocalDate repeatEndDate = request.getRepeatEndDate() == null ? null :
                parseDate(request.getRepeatEndDate());

        CreateTaskCommand command = new CreateTaskCommand(
                userId,
                requestId.trim(),
                request.getTitle(),
                request.getPriority(),
                date,
                request.getRepeatType(),
                request.getRepeatConfig(),
                repeatEndDate
        );

        CreateTaskResult result = taskApplicationService.createTask(command);
        Object repeatConfig = result.getRepeatConfig() == null ? null : JSON.parse(result.getRepeatConfig());
        String repeatEndDateStr = result.getRepeatEndDate() == null ? null :
                result.getRepeatEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE);

        CreateTaskResponse response = new CreateTaskResponse(
                result.getTaskId(),
                result.getTitle(),
                result.getPriority(),
                result.getStatus(),
                result.getDate(),
                result.getRepeatType(),
                repeatConfig,
                repeatEndDateStr,
                DateTimeFormatter.ISO_INSTANT.format(result.getCreatedAt())
        );

        return ApiResponse.success(response);
    }

    /**
     * 创建子任务
     * POST /api/v1/tasks/{taskId}/subtasks
     */
    @PostMapping("/{taskId}/subtasks")
    public ApiResponse<CreateSubTaskResponse> createSubTask(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Request-Id") String requestId,
            @PathVariable("taskId") String taskId,
            @Valid @RequestBody CreateSubTaskRequest request
    ) {
        log.info("收到创建子任务请求: parentTaskId={}, title={}", taskId, request.getTitle());

        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        String accessToken = extractAccessToken(authorization);
        authApplicationService.validateSession(accessToken);
        String userId = jwtService.getUserIdFromToken(accessToken);

        CreateSubTaskResult result = taskApplicationService.createSubTask(
                taskId,
                userId,
                requestId.trim(),
                request.getTitle(),
                request.getRepeatType(),
                request.getRepeatConfig()
        );

        CreateSubTaskResponse response = new CreateSubTaskResponse(
                result.getSubTaskId(),
                result.getParentTaskId(),
                result.getTitle(),
                result.getStatus(),
                result.getRepeatType(),
                DateTimeFormatter.ISO_INSTANT.format(result.getCreatedAt())
        );

        return ApiResponse.success(response);
    }

    /**
     * 实例化虚拟任务
     * POST /api/v1/tasks/{taskId}/materialize
     */
    @PostMapping("/{taskId}/materialize")
    public ApiResponse<MaterializeResponse> materializeTask(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("taskId") String taskId
    ) {
        log.info("收到实例化虚拟任务请求: taskId={}", taskId);

        String accessToken = extractAccessToken(authorization);
        authApplicationService.validateSession(accessToken);
        String userId = jwtService.getUserIdFromToken(accessToken);

        MaterializeResult result = taskApplicationService.materializeVirtualTask(taskId, userId);
        Object repeatConfig = result.getRepeatConfig() == null ? null : JSON.parse(result.getRepeatConfig());

        MaterializeResponse response = new MaterializeResponse(
                result.getTaskId(),
                result.getTitle(),
                result.getPriority(),
                result.getStatus(),
                result.getDate(),
                result.getRepeatType(),
                repeatConfig,
                result.isRepeatInstance(),
                result.getRepeatParentId(),
                DateTimeFormatter.ISO_INSTANT.format(result.getCreatedAt())
        );

        return ApiResponse.success(response);
    }

    /**
     * 完成/反完成任务
     * POST /api/v1/tasks/{taskId}/toggle-complete
     */
    @PostMapping("/{taskId}/toggle-complete")
    public ApiResponse<ToggleCompleteResponse> toggleComplete(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Request-Id") String requestId,
            @PathVariable("taskId") String taskId,
            @Valid @RequestBody ToggleCompleteRequest request
    ) {
        log.info("收到完成/反完成任务请求: taskId={}, completed={}", taskId, request.getCompleted());

        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        String accessToken = extractAccessToken(authorization);
        authApplicationService.validateSession(accessToken);
        String userId = jwtService.getUserIdFromToken(accessToken);

        ToggleCompleteResult result = taskApplicationService.toggleComplete(
                taskId, userId, request.getCompleted());

        String completedAt = result.getCompletedAt() == null ? null :
                DateTimeFormatter.ISO_INSTANT.format(result.getCompletedAt());

        ToggleCompleteResponse response = new ToggleCompleteResponse(
                result.getTaskId(),
                result.getStatus(),
                completedAt
        );

        return ApiResponse.success(response);
    }

    /**
     * 更新任务
     * PUT /api/v1/tasks/{taskId}
     */
    @PutMapping("/{taskId}")
    public ApiResponse<UpdateTaskResponse> updateTask(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Request-Id") String requestId,
            @PathVariable("taskId") String taskId,
            @RequestBody UpdateTaskRequest request
    ) {
        log.info("收到更新任务请求: taskId={}", taskId);

        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        String accessToken = extractAccessToken(authorization);
        authApplicationService.validateSession(accessToken);
        String userId = jwtService.getUserIdFromToken(accessToken);

        // 解析日期
        LocalDate date = request.getDate() == null ? null : parseDate(request.getDate());

        UpdateTaskResult result = taskApplicationService.updateTask(
                taskId,
                userId,
                request.getTitle(),
                request.getPriority(),
                date,
                request.getRepeatType(),
                request.getRepeatConfig()
        );

        Object repeatConfig = result.getRepeatConfig() == null ? null : JSON.parse(result.getRepeatConfig());

        UpdateTaskResponse response = new UpdateTaskResponse(
                result.getTaskId(),
                result.getTitle(),
                result.getPriority(),
                result.getDate(),
                result.getRepeatType(),
                repeatConfig,
                DateTimeFormatter.ISO_INSTANT.format(result.getUpdatedAt())
        );

        return ApiResponse.success(response);
    }

    /**
     * 删除任务
     * DELETE /api/v1/tasks/{taskId}
     */
    @DeleteMapping("/{taskId}")
    public ApiResponse<DeleteTaskResponse> deleteTask(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Request-Id") String requestId,
            @PathVariable("taskId") String taskId,
            @RequestBody(required = false) DeleteTaskRequest request
    ) {
        log.info("收到删除任务请求: taskId={}", taskId);

        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        String accessToken = extractAccessToken(authorization);
        authApplicationService.validateSession(accessToken);
        String userId = jwtService.getUserIdFromToken(accessToken);

        // 处理请求体为空的情况
        boolean deleteAll = false;
        if (request != null && request.getDeleteAll() != null) {
            deleteAll = request.getDeleteAll();
        }

        DeleteTaskResult result = taskApplicationService.deleteTask(taskId, userId, deleteAll);

        DeleteTaskResponse response = new DeleteTaskResponse(result.getDeletedCount());

        return ApiResponse.success(response);
    }

    /**
     * 解析日期字符串
     */
    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ErrorCode.TASK_DATE_OUT_OF_RANGE);
        }
    }

    /**
     * 从 Authorization header 中提取 access_token
     */
    private String extractAccessToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

    /**
     * 将应用层结果转换为接口层响应
     */
    private TaskListResponse convertToResponse(TaskListResult result) {
        Map<String, List<TaskResponse>> tasksByPriority = new LinkedHashMap<String, List<TaskResponse>>();

        for (Map.Entry<String, List<TaskWithSubTasksDTO>> entry : result.getTasks().entrySet()) {
            String priority = entry.getKey();
            List<TaskWithSubTasksDTO> taskDTOs = entry.getValue();
            List<TaskResponse> taskResponses = new ArrayList<TaskResponse>();

            for (TaskWithSubTasksDTO taskDTO : taskDTOs) {
                List<SubTaskResponse> subTaskResponses = new ArrayList<SubTaskResponse>();
                for (SubTaskDTO subTaskDTO : taskDTO.getSubTasks()) {
                    String completedAt = subTaskDTO.getCompletedAt() == null ? null :
                            DateTimeFormatter.ISO_INSTANT.format(subTaskDTO.getCompletedAt());
                    subTaskResponses.add(new SubTaskResponse(
                            subTaskDTO.getId(),
                            subTaskDTO.getParentId(),
                            subTaskDTO.getTitle(),
                            subTaskDTO.getStatus(),
                            subTaskDTO.getRepeatType(),
                            completedAt
                    ));
                }

                String createdAt = DateTimeFormatter.ISO_INSTANT.format(taskDTO.getCreatedAt());
                String completedAt = taskDTO.getCompletedAt() == null ? null :
                        DateTimeFormatter.ISO_INSTANT.format(taskDTO.getCompletedAt());
                Object repeatConfig = taskDTO.getRepeatConfig() == null ? null :
                        JSON.parse(taskDTO.getRepeatConfig());

                taskResponses.add(new TaskResponse(
                        taskDTO.getId(),
                        taskDTO.getTitle(),
                        taskDTO.getPriority(),
                        taskDTO.getStatus(),
                        taskDTO.getDate(),
                        createdAt,
                        completedAt,
                        taskDTO.getRepeatType(),
                        repeatConfig,
                        taskDTO.isRepeatInstance(),
                        taskDTO.getRepeatParentId(),
                        subTaskResponses
                ));
            }

            tasksByPriority.put(priority, taskResponses);
        }

        return new TaskListResponse(
                result.getDate(),
                result.getHasUncheckedTasks(),
                tasksByPriority
        );
    }
}
