package com.gxl.plancore.focus.interfaces.controller;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ApiResponse;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.common.service.JwtService;
import com.gxl.plancore.focus.application.command.EndFocusCommand;
import com.gxl.plancore.focus.application.command.StartFocusCommand;
import com.gxl.plancore.focus.application.dto.EndFocusResult;
import com.gxl.plancore.focus.application.dto.StartFocusResult;
import com.gxl.plancore.focus.application.service.FocusApplicationService;
import com.gxl.plancore.focus.domain.entity.FocusStats;
import com.gxl.plancore.focus.interfaces.dto.EndFocusRequest;
import com.gxl.plancore.focus.interfaces.dto.EndFocusResponse;
import com.gxl.plancore.focus.interfaces.dto.StartFocusRequest;
import com.gxl.plancore.focus.interfaces.dto.StartFocusResponse;
import com.gxl.plancore.focus.interfaces.dto.TotalFocusTimeResponse;
import com.gxl.plancore.user.application.service.AuthApplicationService;

import java.time.format.DateTimeFormatter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 专注控制器
 */
@RestController
@RequestMapping("/api/v1/focus")
public class FocusController {

    private static final Logger log = LoggerFactory.getLogger(FocusController.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final FocusApplicationService focusApplicationService;
    private final AuthApplicationService authApplicationService;
    private final JwtService jwtService;

    public FocusController(FocusApplicationService focusApplicationService,
                           AuthApplicationService authApplicationService,
                           JwtService jwtService) {
        this.focusApplicationService = focusApplicationService;
        this.authApplicationService = authApplicationService;
        this.jwtService = jwtService;
    }

    /**
     * 开始专注
     * POST /api/v1/focus/start
     */
    @PostMapping("/start")
    public ApiResponse<StartFocusResponse> startFocus(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody StartFocusRequest request
    ) {
        log.info("收到开始专注请求: durationSeconds={}, type={}", request.getDurationSeconds(), request.getType());

        // 从 Authorization header 中提取 access_token
        String accessToken = extractAccessToken(authorization);

        // 验证会话有效性
        authApplicationService.validateSession(accessToken);

        // 从 token 中解析用户ID
        String userId = jwtService.getUserIdFromToken(accessToken);

        // 构建命令并执行
        StartFocusCommand command = new StartFocusCommand(
                userId,
                request.getDurationSeconds(),
                request.getType()
        );
        StartFocusResult result = focusApplicationService.startFocus(command);

        // 构建响应
        StartFocusResponse response = new StartFocusResponse(
                result.getSessionId(),
                result.getDurationSeconds(),
                result.getType(),
                DateTimeFormatter.ISO_INSTANT.format(result.getStartAt()),
                DateTimeFormatter.ISO_INSTANT.format(result.getExpectedEndAt())
        );

        return ApiResponse.success(response);
    }

    /**
     * 结束专注
     * POST /api/v1/focus/{sessionId}/end
     * 幂等：已结束的会话重复调用返回相同结果
     */
    @PostMapping("/{sessionId}/end")
    public ApiResponse<EndFocusResponse> endFocus(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("sessionId") String sessionId,
            @Valid @RequestBody EndFocusRequest request
    ) {
        log.info("收到结束专注请求: sessionId={}, elapsedSeconds={}, endType={}",
                sessionId, request.getElapsedSeconds(), request.getEndType());

        // 从 Authorization header 中提取 access_token
        String accessToken = extractAccessToken(authorization);

        // 验证会话有效性
        authApplicationService.validateSession(accessToken);

        // 从 token 中解析用户ID
        String userId = jwtService.getUserIdFromToken(accessToken);

        // 构建命令并执行
        EndFocusCommand command = new EndFocusCommand(
                userId,
                sessionId,
                request.getElapsedSeconds(),
                request.getEndType()
        );
        EndFocusResult result = focusApplicationService.endFocus(command);

        // 构建响应
        EndFocusResponse response = new EndFocusResponse(
                result.getSessionId(),
                result.isCounted(),
                result.getCountedSeconds(),
                result.getTotalFocusTime()
        );

        return ApiResponse.success(response);
    }

    /**
     * 查询总专注时间
     * GET /api/v1/focus/total-time
     */
    @GetMapping("/total-time")
    public ApiResponse<TotalFocusTimeResponse> getTotalFocusTime(
            @RequestHeader("Authorization") String authorization
    ) {
        log.info("收到查询总专注时间请求");

        // 从 Authorization header 中提取 access_token
        String accessToken = extractAccessToken(authorization);

        // 验证会话有效性
        authApplicationService.validateSession(accessToken);

        // 从 token 中解析用户ID
        String userId = jwtService.getUserIdFromToken(accessToken);

        // 查询统计数据
        FocusStats stats = focusApplicationService.queryTotalFocusTime(userId);

        // 构建响应
        TotalFocusTimeResponse response = new TotalFocusTimeResponse(
                stats.getTotalSeconds(),
                stats.getTotalHours()
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
}
