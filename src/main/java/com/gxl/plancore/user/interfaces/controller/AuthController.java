package com.gxl.plancore.user.interfaces.controller;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ApiResponse;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.common.service.JwtService;
import com.gxl.plancore.user.application.command.ChangePasswordCommand;
import com.gxl.plancore.user.application.command.ForgotPasswordCommand;
import com.gxl.plancore.user.application.command.LoginCommand;
import com.gxl.plancore.user.application.command.RegisterCommand;
import com.gxl.plancore.user.application.dto.DeviceDTO;
import com.gxl.plancore.user.application.dto.DeviceListDTO;
import com.gxl.plancore.user.application.dto.LoginResult;
import com.gxl.plancore.user.application.dto.RefreshResult;
import com.gxl.plancore.user.application.dto.UserDTO;
import com.gxl.plancore.user.application.service.AuthApplicationService;
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
import com.gxl.plancore.user.interfaces.dto.UserInfoResponse;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final AuthApplicationService authApplicationService;
    private final JwtService jwtService;
    
    public AuthController(AuthApplicationService authApplicationService, JwtService jwtService) {
        this.authApplicationService = authApplicationService;
        this.jwtService = jwtService;
    }
    
    /**
     * 用户登录
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
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
                ipAddress
        );
        
        // 执行登录
        LoginResult loginResult = authApplicationService.login(command);
        
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
                        null  // IP 归属地需要单独查询
                ))
                .entitlement(entitlement)
                .build();
        
        return ApiResponse.success(response);
    }
    
    /**
     * 用户注册
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("收到注册请求: email={}", request.getEmail());
        
        // 获取客户端 IP
        String ipAddress = getClientIp(httpRequest);
        
        // 构建命令对象
        RegisterCommand command = new RegisterCommand(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                ipAddress
        );
        
        // 执行注册
        UserDTO userDTO = authApplicationService.register(command);
        
        // 构建响应（暂时不生成 token，后续添加 JWT 功能）
        AuthResponse response = AuthResponse.builder()
                .userId(userDTO.getUserId())
                .accessToken("") // TODO: 生成 JWT token
                .refreshToken("") // TODO: 生成 refresh token
                .expiresIn(7200)
                .userInfo(new UserInfoResponse(
                        userDTO.getNickname(),
                        userDTO.getAvatar(),
                        userDTO.getIpLocation()
                ))
                .build();
        
        return ApiResponse.success(response);
    }
    
    /**
     * 找回密码
     * POST /api/v1/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ApiResponse<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        log.info("收到找回密码请求: email={}", request.getEmail());
        
        // 构建命令对象
        ForgotPasswordCommand command = new ForgotPasswordCommand(request.getEmail());
        
        // 执行找回密码
        authApplicationService.forgotPassword(command);
        
        // 返回成功响应
        return ApiResponse.success(ForgotPasswordResponse.success());
    }
    
    /**
     * 刷新 Token
     * POST /api/v1/auth/refresh
     * 使用 refresh_token 获取新的 access_token 和 refresh_token
     */
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("收到刷新 Token 请求");
        
        // 执行刷新 Token
        RefreshResult result = authApplicationService.refreshToken(request.getRefreshToken());
        
        // 构建响应
        RefreshTokenResponse response = new RefreshTokenResponse(
                result.getAccessToken(),
                result.getRefreshToken(),
                result.getExpiresIn()
        );
        
        return ApiResponse.success(response);
    }
    
    /**
     * 退出登录
     * POST /api/v1/auth/logout
     * 幂等：重复退出返回成功
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authorization
    ) {
        log.info("收到退出登录请求");
        
        // 从 Authorization header 中提取 access_token
        String accessToken = extractAccessToken(authorization);
        
        // 从 token 中解析用户ID和设备ID
        String userId;
        String deviceId;
        try {
            userId = jwtService.getUserIdFromToken(accessToken);
            deviceId = jwtService.getDeviceIdFromToken(accessToken);
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        // 执行退出登录
        authApplicationService.logout(userId, deviceId);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 检查会话状态
     * GET /api/v1/auth/session/check
     * 供前端定时轮询，检查当前会话是否仍然有效
     */
    @GetMapping("/session/check")
    public ApiResponse<SessionStatusResponse> checkSession(
            @RequestHeader("Authorization") String authorization
    ) {
        log.info("收到检查会话状态请求");
        
        // 从 Authorization header 中提取 access_token
        String accessToken = extractAccessToken(authorization);
        
        // 验证会话有效性（检查数据库中会话状态）
        // 如果会话无效，validateSession 会抛出 UNAUTHORIZED 异常
        authApplicationService.validateSession(accessToken);
        
        // 从 token 中解析用户ID和设备ID
        String userId = jwtService.getUserIdFromToken(accessToken);
        String deviceId = jwtService.getDeviceIdFromToken(accessToken);
        
        // 返回会话有效的响应
        SessionStatusResponse response = SessionStatusResponse.valid(userId, deviceId);
        
        return ApiResponse.success(response);
    }
    
    /**
     * 查询设备列表
     * GET /api/v1/auth/devices
     */
    @GetMapping("/devices")
    public ApiResponse<DeviceListResponse> getDevices(
            @RequestHeader("Authorization") String authorization
    ) {
        log.info("收到查询设备列表请求");
        
        // 从 Authorization header 中提取 access_token
        String accessToken = extractAccessToken(authorization);
        
        // 验证会话有效性（检查数据库中会话状态）
        authApplicationService.validateSession(accessToken);
        
        // 从 token 中解析用户ID和设备ID
        String userId = jwtService.getUserIdFromToken(accessToken);
        String currentDeviceId = jwtService.getDeviceIdFromToken(accessToken);
        
        // 查询设备列表
        DeviceListDTO deviceListDTO = authApplicationService.getDevices(userId, currentDeviceId);
        
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
     * 踢出指定设备
     * POST /api/v1/auth/devices/{deviceId}/logout
     * 幂等：重复踢出返回成功
     */
    @PostMapping("/devices/{deviceId}/logout")
    public ApiResponse<Void> logoutDevice(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("deviceId") String targetDeviceId
    ) {
        log.info("收到踢出设备请求: targetDeviceId={}", targetDeviceId);
        
        // 从 Authorization header 中提取 access_token
        String accessToken = extractAccessToken(authorization);
        
        // 验证会话有效性（检查数据库中会话状态）
        authApplicationService.validateSession(accessToken);
        
        // 从 token 中解析用户ID和当前设备ID
        String userId = jwtService.getUserIdFromToken(accessToken);
        String currentDeviceId = jwtService.getDeviceIdFromToken(accessToken);
        
        // 执行踢出设备
        authApplicationService.logoutDevice(userId, currentDeviceId, targetDeviceId);
        
        return ApiResponse.success(null);
    }
    
    /**
     * 修改密码
     * POST /api/v1/auth/password
     * 修改成功后，其他设备会话将被强制下线
     */
    @PostMapping("/password")
    public ApiResponse<ChangePasswordResponse> changePassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        log.info("收到修改密码请求");
        
        // 从 Authorization header 中提取 access_token
        String accessToken = extractAccessToken(authorization);
        
        // 验证会话有效性（检查数据库中会话状态）
        authApplicationService.validateSession(accessToken);
        
        // 从 token 中解析用户ID和当前设备ID
        String userId = jwtService.getUserIdFromToken(accessToken);
        String currentDeviceId = jwtService.getDeviceIdFromToken(accessToken);
        
        // 构建命令并执行修改密码
        ChangePasswordCommand command = new ChangePasswordCommand(
                userId,
                currentDeviceId,
                request.getOldPassword(),
                request.getNewPassword()
        );
        authApplicationService.changePassword(command);
        
        return ApiResponse.success(ChangePasswordResponse.success());
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
                dto.isCurrent()
        );
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
}
