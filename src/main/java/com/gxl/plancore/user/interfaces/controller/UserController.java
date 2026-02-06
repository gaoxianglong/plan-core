package com.gxl.plancore.user.interfaces.controller;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ApiResponse;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.common.service.JwtService;
import com.gxl.plancore.user.application.command.UpdateProfileCommand;
import com.gxl.plancore.user.application.service.AuthApplicationService;
import com.gxl.plancore.user.application.service.UserApplicationService;
import com.gxl.plancore.user.domain.entity.User;
import com.gxl.plancore.user.interfaces.dto.UpdateProfileRequest;
import com.gxl.plancore.user.interfaces.dto.UpdateProfileResponse;
import com.gxl.plancore.user.interfaces.dto.UserProfileResponse;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户中心控制器
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserApplicationService userApplicationService;
    private final AuthApplicationService authApplicationService;
    private final JwtService jwtService;

    public UserController(UserApplicationService userApplicationService,
                          AuthApplicationService authApplicationService,
                          JwtService jwtService) {
        this.userApplicationService = userApplicationService;
        this.authApplicationService = authApplicationService;
        this.jwtService = jwtService;
    }

    /**
     * 查询用户信息
     * GET /api/v1/user/profile
     */
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getUserProfile(
            @RequestHeader("Authorization") String authorization
    ) {
        log.info("收到查询用户信息请求");

        // 从 Authorization header 中提取 access_token
        String accessToken = extractAccessToken(authorization);

        // 验证会话有效性
        authApplicationService.validateSession(accessToken);

        // 从 token 中解析用户ID
        String userId = jwtService.getUserIdFromToken(accessToken);

        // 查询用户信息
        User user = userApplicationService.getUserProfile(userId);

        // 构建响应
        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(user.getUserId().getValue());
        response.setNickname(user.getNickname().getValue());
        response.setAvatar(user.getAvatar());
        response.setIpLocation(user.getIpLocation());
        response.setConsecutiveDays(user.getConsecutiveDays());
        
        // lastCheckInDate 格式：YYYY-MM-DD
        if (user.getLastCheckInDate() != null) {
            response.setLastCheckInDate(user.getLastCheckInDate().toString());
        }
        
        response.setNicknameModifyCount(user.getNicknameModifyCount());
        
        // nicknameNextModifyAt：首次修改时间 + 7天后可再次修改
        // 如果从未修改过昵称，或者7天窗口已过，则不返回限制时间
        if (user.getNicknameFirstModifyAt() != null) {
            Instant windowEnd = user.getNicknameFirstModifyAt().plusSeconds(7 * 24 * 60 * 60);
            if (Instant.now().isBefore(windowEnd) && user.getNicknameModifyCount() >= 2) {
                response.setNicknameNextModifyAt(DateTimeFormatter.ISO_INSTANT.format(windowEnd));
            }
        }

        return ApiResponse.success(response);
    }

    /**
     * 更新用户信息（昵称和/或头像）
     * PUT /api/v1/user/profile
     */
    @PutMapping("/profile")
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("收到更新用户资料请求");

        // 从 Authorization header 中提取 access_token
        String accessToken = extractAccessToken(authorization);

        // 验证会话有效性（检查数据库中会话状态）
        authApplicationService.validateSession(accessToken);

        // 从 token 中解析用户ID
        String userId = jwtService.getUserIdFromToken(accessToken);

        // 校验至少传了一个字段
        if (!hasValue(request.getNickname()) && !hasValue(request.getAvatar())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 构建命令并执行
        UpdateProfileCommand command = new UpdateProfileCommand(
                userId,
                request.getNickname(),
                request.getAvatar()
        );
        User updatedUser = userApplicationService.updateProfile(command);

        // 构建响应
        String updatedAtStr = DateTimeFormatter.ISO_INSTANT.format(updatedUser.getUpdatedAt());
        UpdateProfileResponse response = new UpdateProfileResponse(
                updatedUser.getNickname().getValue(),
                updatedUser.getAvatar(),
                updatedAtStr
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
     * 判断字符串是否有值
     */
    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
