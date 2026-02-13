package com.gxl.plancore.user.interfaces.controller;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ApiResponse;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.user.application.command.ChangePasswordCommand;
import com.gxl.plancore.user.application.command.ForgotPasswordCommand;
import com.gxl.plancore.user.application.command.LoginCommand;
import com.gxl.plancore.user.application.command.RegisterCommand;
import com.gxl.plancore.user.application.command.UpdateProfileCommand;
import com.gxl.plancore.user.application.dto.DeviceDTO;
import com.gxl.plancore.user.application.dto.DeviceListDTO;
import com.gxl.plancore.user.application.dto.LoginResult;
import com.gxl.plancore.user.application.dto.RefreshResult;
import com.gxl.plancore.user.application.dto.UserDTO;
import com.gxl.plancore.user.application.service.AuthService;
import com.gxl.plancore.user.application.service.UserApplicationService;
import com.gxl.plancore.user.domain.entity.User;
import com.gxl.plancore.user.interfaces.dto.AuthResponse;
import com.gxl.plancore.user.interfaces.dto.ChangePasswordRequest;
import com.gxl.plancore.user.interfaces.dto.ChangePasswordResponse;
import com.gxl.plancore.user.interfaces.dto.DeviceInfoRequest;
import com.gxl.plancore.user.interfaces.dto.DeviceListResponse;
import com.gxl.plancore.user.interfaces.dto.DeviceResponse;
import com.gxl.plancore.user.interfaces.dto.EntitlementResponse;
import com.gxl.plancore.user.interfaces.dto.ForgotPasswordRequest;
import com.gxl.plancore.user.interfaces.dto.ForgotPasswordResponse;
import com.gxl.plancore.user.interfaces.dto.LoginRequest;
import com.gxl.plancore.user.interfaces.dto.RefreshTokenRequest;
import com.gxl.plancore.user.interfaces.dto.RefreshTokenResponse;
import com.gxl.plancore.user.interfaces.dto.RegisterRequest;
import com.gxl.plancore.user.interfaces.dto.SessionStatusResponse;
import com.gxl.plancore.user.interfaces.dto.UpdateProfileRequest;
import com.gxl.plancore.user.interfaces.dto.UpdateProfileResponse;
import com.gxl.plancore.user.interfaces.dto.UserInfoResponse;
import com.gxl.plancore.user.interfaces.dto.UserProfileResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final AuthService authService;

    public UserController(UserApplicationService userApplicationService,
            AuthService authService) {
        this.userApplicationService = userApplicationService;
        this.authService = authService;
    }

    /**
     * 查询用户信息
     * GET /api/v1/user/profile
     */
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getUserProfile(
            HttpServletRequest httpRequest) {
        log.info("收到查询用户信息请求");

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");

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
            HttpServletRequest httpRequest,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("收到更新用户资料请求");

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");

        // 校验至少传了一个字段
        if (!hasValue(request.getNickname()) && !hasValue(request.getAvatar())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 构建命令并执行
        UpdateProfileCommand command = new UpdateProfileCommand(
                userId,
                request.getNickname(),
                request.getAvatar());
        User updatedUser = userApplicationService.updateProfile(command);

        // 构建响应
        String updatedAtStr = DateTimeFormatter.ISO_INSTANT.format(updatedUser.getUpdatedAt());
        UpdateProfileResponse response = new UpdateProfileResponse(
                updatedUser.getNickname().getValue(),
                updatedUser.getAvatar(),
                updatedAtStr);

        return ApiResponse.success(response);
    }

    /**
     * 用户登录
     * POST /api/v1/user/login
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("收到登录请求: email={}", request.getEmail());

        // 获取客户端 IP
        String ipAddress = getClientIp(httpRequest);

        // 获取设备信息
        DeviceInfoRequest deviceInfo = request.getDeviceInfo();

        // 构建命令对象
        LoginCommand command = new LoginCommand(
                request.getEmail(),
                request.getPassword(),
                deviceInfo.getDeviceId(),
                deviceInfo.getDeviceName(),
                deviceInfo.getPlatform(),
                deviceInfo.getOsVersion(),
                deviceInfo.getAppVersion(),
                ipAddress);

        // 执行登录
        LoginResult loginResult = userApplicationService.login(command);

        // 计算 Token 过期时间（秒）
        long expiresIn = Duration.between(Instant.now(), loginResult.getExpiresAt()).getSeconds();

        // 构建会员权益信息（免费期：暂时使用固定值，后续从数据库读取）
        // TODO: 从 entitlement 表查询用户权益
        Instant trialStartAt = Instant.now();
        Instant expireAt = trialStartAt.plus(30, ChronoUnit.DAYS);
        String trialStartAtStr = DateTimeFormatter.ISO_INSTANT.format(trialStartAt);
        String expireAtStr = DateTimeFormatter.ISO_INSTANT.format(expireAt);
        EntitlementResponse entitlement = EntitlementResponse.freeTrial(trialStartAtStr, expireAtStr);

        // 构建响应
        AuthResponse response = AuthResponse.builder()
                .userId(loginResult.getUserId())
                .accessToken(loginResult.getAccessToken())
                .refreshToken(loginResult.getRefreshToken())
                .expiresIn(expiresIn)
                .userInfo(new UserInfoResponse(
                        loginResult.getNickname(),
                        loginResult.getAvatar(),
                        null // IP 归属地需要单独查询
                ))
                .entitlement(entitlement)
                .build();

        return ApiResponse.success(response);
    }

    /**
     * 用户注册
     * POST /api/v1/user/register
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        log.info("收到注册请求: email={}", request.getEmail());

        // 获取客户端 IP
        String ipAddress = getClientIp(httpRequest);

        // 构建命令对象
        RegisterCommand command = new RegisterCommand(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                ipAddress);

        // 执行注册
        UserDTO userDTO = userApplicationService.register(command);

        // 构建响应（暂时不生成 token，后续添加 JWT 功能）
        AuthResponse response = AuthResponse.builder()
                .userId(userDTO.getUserId())
                .expiresIn(7200)
                .userInfo(new UserInfoResponse(
                        userDTO.getNickname(),
                        userDTO.getAvatar(),
                        userDTO.getIpLocation()))
                .build();

        return ApiResponse.success(response);
    }

    /**
     * 找回密码
     * POST /api/v1/user/forgot-password
     */
    @PostMapping("/forgot-password")
    public ApiResponse<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        log.info("收到找回密码请求: email={}", request.getEmail());

        // 构建命令对象
        ForgotPasswordCommand command = new ForgotPasswordCommand(request.getEmail());

        // 执行找回密码
        userApplicationService.forgotPassword(command);

        // 返回成功响应
        return ApiResponse.success(ForgotPasswordResponse.success());
    }

    /**
     * 基于RefreshToken刷新 AccessToken
     * POST /api/v1/user/refreshAccessToken
     * 使用 refresh_token 获取新的 access_token 和 refresh_token
     */
    @PostMapping("/refreshAccessToken")
    public ApiResponse<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("== 收到刷新 Token 请求 ==");

        // 执行刷新 Token
        RefreshResult result = authService.refreshAccessToken(request.getRefreshToken());

        // 构建响应
        RefreshTokenResponse response = new RefreshTokenResponse(
                result.getAccessToken(),
                result.getRefreshToken(),
                result.getExpiresIn());

        return ApiResponse.success(response);
    }

    /**
     * 退出登录
     * POST /api/v1/user/logout
     * 幂等：重复退出返回成功
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            HttpServletRequest httpRequest) {
        log.info("收到退出登录请求");

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");
        String deviceId = (String) httpRequest.getAttribute("deviceId");

        // 执行退出登录
        userApplicationService.logout(userId, deviceId);

        return ApiResponse.success(null);
    }

    /**
     * 检查RefreshToken是否有效 & 会话状态
     * GET /api/v1/user/session/check
     * 供前端定时轮询，检查当前会话是否仍然有效
     */
    @GetMapping("/session/check")
    public ApiResponse<SessionStatusResponse> checkSession(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("收到检查会话状态请求");

        String refreshToken = request.getRefreshToken();

        // 验证会话有效性（检查数据库中会话状态）
        // 如果会话无效，validateSession 会抛出 UNAUTHORIZED 异常
        authService.validateRefreshToken(refreshToken);

        // 从 token 中解析用户ID和设备ID
        String userId = authService.getUserIdFromToken(refreshToken);
        String deviceId = authService.getDeviceIdFromToken(refreshToken);

        // 返回会话有效的响应
        SessionStatusResponse response = SessionStatusResponse.valid(userId, deviceId);

        return ApiResponse.success(response);
    }

    /**
     * 查询用户的设备列表
     * GET /api/v1/user/devices
     */
    @GetMapping("/devices")
    public ApiResponse<DeviceListResponse> getDevices(
            HttpServletRequest httpRequest) {
        log.info("收到查询设备列表请求");

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");
        String deviceId = (String) httpRequest.getAttribute("deviceId");

        // 查询设备列表
        DeviceListDTO deviceListDTO = userApplicationService.getDevices(userId, deviceId);

        // 转换为响应 DTO
        DeviceResponse currentDevice = null;
        if (deviceListDTO.getCurrentDevice() != null) {
            currentDevice = toDeviceResponse(deviceListDTO.getCurrentDevice());
        }

        List<DeviceResponse> otherDevices = new ArrayList<DeviceResponse>();
        for (DeviceDTO dto : deviceListDTO.getOtherDevices()) {
            otherDevices.add(toDeviceResponse(dto));
        }

        DeviceListResponse response = new DeviceListResponse(currentDevice, otherDevices);

        return ApiResponse.success(response);
    }

    /**
     * 用户踢出指定设备
     * POST /api/v1/user/devices/{deviceId}/logout
     * 幂等：重复踢出返回成功
     */
    @PostMapping("/devices/{deviceId}/logout")
    public ApiResponse<Void> logoutDevice(
            HttpServletRequest httpRequest,
            @PathVariable("deviceId") String targetDeviceId) {
        log.info("收到踢出设备请求: targetDeviceId={}", targetDeviceId);

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");
        String deviceId = (String) httpRequest.getAttribute("deviceId");

        // 执行踢出设备
        userApplicationService.logoutDevice(userId, deviceId, targetDeviceId);

        return ApiResponse.success(null);
    }

    /**
     * 修改密码
     * POST /api/v1/user/password
     * 修改成功后，其他设备会话将被强制下线
     */
    @PostMapping("/password")
    public ApiResponse<ChangePasswordResponse> changePassword(
            HttpServletRequest httpRequest,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("收到修改密码请求");

        // 从拦截器设置的请求属性中获取用户ID
        String userId = (String) httpRequest.getAttribute("userId");
        String deviceId = (String) httpRequest.getAttribute("deviceId");

        // 构建命令并执行修改密码
        ChangePasswordCommand command = new ChangePasswordCommand(
                userId,
                deviceId,
                request.getOldPassword(),
                request.getNewPassword());
        userApplicationService.changePassword(command);

        return ApiResponse.success(ChangePasswordResponse.success());
    }

    /**
     * 将应用层 DTO 转换为接口层响应 DTO
     */
    private DeviceResponse toDeviceResponse(DeviceDTO dto) {
        String lastLoginAtStr = null;
        if (dto.getLastLoginAt() != null) {
            lastLoginAtStr = DateTimeFormatter.ISO_INSTANT.format(dto.getLastLoginAt());
        }
        return new DeviceResponse(
                dto.getDeviceId(),
                dto.getDeviceName(),
                dto.getPlatform(),
                dto.getLastLoginIp(),
                lastLoginAtStr,
                dto.isCurrent());
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
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
