package com.gxl.plancore.task.interfaces.controller;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ApiResponse;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.common.service.JwtService;
import com.gxl.plancore.task.application.dto.ToggleSubTaskCompleteResult;
import com.gxl.plancore.task.application.dto.UpdateSubTaskResult;
import com.gxl.plancore.task.application.service.TaskApplicationService;
import com.gxl.plancore.task.interfaces.dto.ToggleSubTaskCompleteRequest;
import com.gxl.plancore.task.interfaces.dto.ToggleSubTaskCompleteResponse;
import com.gxl.plancore.task.interfaces.dto.UpdateSubTaskRequest;
import com.gxl.plancore.task.interfaces.dto.UpdateSubTaskResponse;
import com.gxl.plancore.user.application.service.AuthApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
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

/**
 * 子任务控制器
 * 
 * 处理子任务的更新、删除、完成等操作
 */
@RestController
@RequestMapping("/api/v1/subtasks")
public class SubTaskController {

    private static final Logger log = LoggerFactory.getLogger(SubTaskController.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final TaskApplicationService taskApplicationService;
    private final AuthApplicationService authApplicationService;
    private final JwtService jwtService;

    /**
     * 构造子任务控制器
     */
    public SubTaskController(TaskApplicationService taskApplicationService,
                              AuthApplicationService authApplicationService,
                              JwtService jwtService) {
        this.taskApplicationService = taskApplicationService;
        this.authApplicationService = authApplicationService;
        this.jwtService = jwtService;
    }

    /**
     * 更新子任务
     * PUT /api/v1/subtasks/{subTaskId}
     */
    @PutMapping("/{subTaskId}")
    public ApiResponse<UpdateSubTaskResponse> updateSubTask(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Request-Id") String requestId,
            @PathVariable("subTaskId") String subTaskId,
            @RequestBody UpdateSubTaskRequest request
    ) {
        log.info("收到更新子任务请求: subTaskId={}, date={}", subTaskId, request.getDate());

        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        String accessToken = extractAccessToken(authorization);
        authApplicationService.validateSession(accessToken);
        String userId = jwtService.getUserIdFromToken(accessToken);

        LocalDate date = parseOptionalDate(request.getDate());

        UpdateSubTaskResult result = taskApplicationService.updateSubTask(
                subTaskId,
                userId,
                request.getTitle(),
                request.getRepeatType(),
                request.getRepeatConfig(),
                date
        );

        UpdateSubTaskResponse response = new UpdateSubTaskResponse(
                result.getSubTaskId(),
                result.getTitle(),
                DateTimeFormatter.ISO_INSTANT.format(result.getUpdatedAt())
        );

        return ApiResponse.success(response);
    }

    /**
     * 删除子任务
     * DELETE /api/v1/subtasks/{subTaskId}
     */
    @DeleteMapping("/{subTaskId}")
    public ApiResponse<Void> deleteSubTask(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Request-Id") String requestId,
            @PathVariable("subTaskId") String subTaskId,
            @RequestParam(value = "date", required = false) String dateStr
    ) {
        log.info("收到删除子任务请求: subTaskId={}, date={}", subTaskId, dateStr);

        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        String accessToken = extractAccessToken(authorization);
        authApplicationService.validateSession(accessToken);
        String userId = jwtService.getUserIdFromToken(accessToken);

        LocalDate date = parseOptionalDate(dateStr);

        taskApplicationService.deleteSubTask(subTaskId, userId, date);

        return ApiResponse.success(null);
    }

    /**
     * 完成/反完成子任务
     * POST /api/v1/subtasks/{subTaskId}/toggle-complete
     */
    @PostMapping("/{subTaskId}/toggle-complete")
    public ApiResponse<ToggleSubTaskCompleteResponse> toggleSubTaskComplete(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Request-Id") String requestId,
            @PathVariable("subTaskId") String subTaskId,
            @Valid @RequestBody ToggleSubTaskCompleteRequest request
    ) {
        log.info("收到完成/反完成子任务请求: subTaskId={}, completed={}, date={}",
                subTaskId, request.getCompleted(), request.getDate());

        if (requestId == null || requestId.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        String accessToken = extractAccessToken(authorization);
        authApplicationService.validateSession(accessToken);
        String userId = jwtService.getUserIdFromToken(accessToken);

        LocalDate date = parseOptionalDate(request.getDate());

        ToggleSubTaskCompleteResult result = taskApplicationService.toggleSubTaskComplete(
                subTaskId, userId, request.getCompleted(), date);

        String completedAt = result.getCompletedAt() == null ? null :
                DateTimeFormatter.ISO_INSTANT.format(result.getCompletedAt());

        ToggleSubTaskCompleteResponse.ParentTaskInfo parentTask =
                new ToggleSubTaskCompleteResponse.ParentTaskInfo(
                        result.getParentTaskId(),
                        result.getParentTaskStatus()
                );

        ToggleSubTaskCompleteResponse response = new ToggleSubTaskCompleteResponse(
                result.getSubTaskId(),
                result.getStatus(),
                completedAt,
                parentTask
        );

        return ApiResponse.success(response);
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
     * 解析可选的日期参数
     */
    private LocalDate parseOptionalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ErrorCode.TASK_DATE_OUT_OF_RANGE);
        }
    }
}
