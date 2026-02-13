package com.gxl.plancore.user.application.service;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.common.service.EmailService;
import com.gxl.plancore.user.application.command.ChangePasswordCommand;
import com.gxl.plancore.user.application.command.ForgotPasswordCommand;
import com.gxl.plancore.user.application.command.LoginCommand;
import com.gxl.plancore.user.application.command.RegisterCommand;
import com.gxl.plancore.user.application.command.UpdateProfileCommand;
import com.gxl.plancore.user.application.dto.DeviceDTO;
import com.gxl.plancore.user.application.dto.DeviceListDTO;
import com.gxl.plancore.user.application.dto.LoginResult;
import com.gxl.plancore.user.application.dto.UserDTO;
import com.gxl.plancore.user.application.service.AuthService.TokenPair;
import com.gxl.plancore.user.domain.entity.DeviceSession;
import com.gxl.plancore.user.domain.entity.User;
import com.gxl.plancore.user.domain.repository.DeviceSessionRepository;
import com.gxl.plancore.user.domain.repository.UserRepository;
import com.gxl.plancore.user.domain.valueobject.Email;
import com.gxl.plancore.user.domain.valueobject.Nickname;
import com.gxl.plancore.user.domain.valueobject.Password;
import com.gxl.plancore.user.domain.valueobject.UserId;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户应用服务
 * 编排用户中心相关业务流程（资料修改等）
 */
@Service
public class UserApplicationService {

    private static final Logger log = LoggerFactory.getLogger(UserApplicationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DeviceSessionRepository deviceSessionRepository;
    private final AuthService authService;
    private final EmailService emailService;

    // 频率限制缓存：邮箱 -> 上次发送时间戳
    private final ConcurrentHashMap<String, Long> rateLimitCache = new ConcurrentHashMap<String, Long>();

    @Value("${app.forgot-password.rate-limit-seconds:60}")
    private int rateLimitSeconds;

    @Value("${app.device.max-sessions-per-user:10}")
    private int maxSessionsPerUser;

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int NEW_PASSWORD_LENGTH = 10;

    public UserApplicationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            DeviceSessionRepository deviceSessionRepository, AuthService authService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.deviceSessionRepository = deviceSessionRepository;
        this.authService = authService;
        this.emailService = emailService;
    }

    /**
     * 用户登录
     * 验证凭证、管理设备会话、生成双 Token
     */
    @Transactional
    public LoginResult login(LoginCommand command) {
        log.info("用户登录: email={}", command.getEmail());

        // 1. 校验邮箱格式并创建值对象
        Email email;
        try {
            email = Email.of(command.getEmail());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        // 2. 查询用户
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            log.warn("登录失败: 邮箱不存在, email={}", command.getEmail());
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        User user = userOptional.get();

        // 3. 验证密码
        boolean passwordMatch = passwordEncoder.matches(command.getPassword(), user.getPassword().getHashedValue());
        if (!passwordMatch) {
            log.warn("登录失败: 密码错误, email={}", command.getEmail());
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        String userId = user.getUserId().getValue();
        String deviceId = command.getDeviceId();

        // 4. 检查该设备是否已有活跃会话
        Optional<DeviceSession> existingSession = deviceSessionRepository.findActiveByUserIdAndDeviceId(userId,
                deviceId);
        if (existingSession.isPresent()) {
            // 如果设备已有会话，先将其设为已退出
            deviceSessionRepository.logoutByUserIdAndDeviceId(userId, deviceId);
            log.info("设备已有会话，已退出旧会话: deviceId={}", deviceId);
        }

        // 5. 检查设备数量限制
        int activeSessionCount = deviceSessionRepository.countActiveByUserId(userId);
        if (activeSessionCount >= maxSessionsPerUser) {
            // 超出限制，踢出最早的设备
            Optional<DeviceSession> oldestSession = deviceSessionRepository.deleteOldestActiveSession(userId);
            if (oldestSession.isPresent()) {
                log.info("设备数量超限，踢出最早设备: sessionId={}, deviceName={}",
                        oldestSession.get().getSessionId(), oldestSession.get().getDeviceName());
            }
        }

        // 6. 生成双 Token
        // 先生成 sessionId 用于 JWT 的 sid 字段
        String sessionId = java.util.UUID.randomUUID().toString();
        TokenPair tokenPair = authService.generateTokens(userId, sessionId, deviceId);
        String accessToken = tokenPair.getAccessToken();
        String refreshToken = tokenPair.getRefreshToken();
        Instant expiresAt = tokenPair.getAccessExpiresAt();
        Instant refreshExpiresAt = tokenPair.getRefreshExpiresAt();

        // 7. 创建设备会话
        DeviceSession deviceSession = DeviceSession.create(
                userId,
                deviceId,
                command.getDeviceName(),
                command.getPlatform(),
                command.getOsVersion(),
                command.getAppVersion(),
                accessToken,
                refreshToken,
                expiresAt,
                refreshExpiresAt,
                command.getIpAddress());

        // 由于 DeviceSession.create 已经生成了 sessionId，需要重新生成 Token 以使用正确的 sessionId
        String actualSessionId = deviceSession.getSessionId();
        accessToken = tokenPair.getAccessToken();
        refreshToken = tokenPair.getRefreshToken();

        // 更新 DeviceSession 的 Token
        deviceSession = DeviceSession.create(
                userId,
                deviceId,
                command.getDeviceName(),
                command.getPlatform(),
                command.getOsVersion(),
                command.getAppVersion(),
                accessToken,
                refreshToken,
                expiresAt,
                refreshExpiresAt,
                command.getIpAddress());

        // 8. 保存设备会话
        deviceSessionRepository.save(deviceSession);

        log.info("用户登录成功: userId={}, email={}, deviceId={}, sessionId={}",
                userId, email.getValue(), deviceId, deviceSession.getSessionId());

        // 9. 返回登录结果
        return new LoginResult(
                userId,
                user.getEmail().getValue(),
                user.getNickname().getValue(),
                user.getAvatar(),
                accessToken,
                refreshToken,
                expiresAt,
                refreshExpiresAt);
    }

    /**
     * 用户注册
     */
    @Transactional
    public UserDTO register(RegisterCommand command) {
        log.info("用户注册: email={}", command.getEmail());

        // 1. 校验密码格式
        try {
            Password.validatePlainText(command.getPassword());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_INVALID);
        }

        // 2. 校验邮箱格式并创建值对象
        Email email;
        try {
            email = Email.of(command.getEmail());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), e.getMessage());
        }

        // 3. 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_EXISTS);
        }

        // 4. 校验昵称
        Nickname nickname;
        try {
            nickname = Nickname.of(command.getNickname());
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("违规词")) {
                throw new BusinessException(ErrorCode.AUTH_NICKNAME_FORBIDDEN);
            }
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), e.getMessage());
        }

        // 5. 加密密码
        String hashedPassword = passwordEncoder.encode(command.getPassword());
        Password password = Password.fromHashed(hashedPassword);

        // 6. 创建用户
        User user = User.create(email, password, nickname);

        // 7. 保存用户
        userRepository.save(user);

        log.info("用户注册成功: userId={}, email={}", user.getUserId().getValue(), email.getValue());

        // 8. 返回用户信息
        return UserDTO.fromDomain(user);
    }

    /**
     * 找回密码
     * 生成新密码并发送到用户邮箱
     */
    @Transactional
    public void forgotPassword(ForgotPasswordCommand command) {
        log.info("找回密码请求: email={}", command.getEmail());

        // 1. 校验邮箱格式并创建值对象
        Email email;
        try {
            email = Email.of(command.getEmail());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_NOT_FOUND);
        }

        // 2. 频率限制检查（同一邮箱 1 分钟内只能发送 1 次）
        String emailKey = email.getValue().toLowerCase();
        Long lastSendTime = rateLimitCache.get(emailKey);
        long currentTime = System.currentTimeMillis();
        if (lastSendTime != null) {
            long elapsedSeconds = (currentTime - lastSendTime) / 1000;
            if (elapsedSeconds < rateLimitSeconds) {
                log.warn("找回密码请求过于频繁: email={}, 剩余等待秒数={}",
                        command.getEmail(), rateLimitSeconds - elapsedSeconds);
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS.getCode(),
                        "操作过于频繁，请" + (rateLimitSeconds - elapsedSeconds) + "秒后重试");
            }
        }

        // 3. 查询用户
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            log.warn("找回密码失败: 邮箱不存在, email={}", command.getEmail());
            throw new BusinessException(ErrorCode.AUTH_EMAIL_NOT_FOUND);
        }

        User user = userOptional.get();

        // 4. 生成新的随机密码
        String newPlainPassword = generateRandomPassword();

        // 5. 加密新密码
        String hashedPassword = passwordEncoder.encode(newPlainPassword);
        Password newPassword = Password.fromHashed(hashedPassword);

        // 6. 更新用户密码
        user.changePassword(newPassword);
        userRepository.updatePassword(user);

        // 7. 发送邮件
        emailService.sendPasswordResetEmail(email.getValue(), newPlainPassword);

        // 8. 更新频率限制缓存
        rateLimitCache.put(emailKey, currentTime);

        log.info("找回密码成功: userId={}, email={}", user.getUserId().getValue(), email.getValue());
    }

    /**
     * 退出当前设备登录
     * 幂等：重复退出返回成功
     * 
     * @param userId   当前用户ID
     * @param deviceId 当前设备ID
     */
    @Transactional
    public void logout(String userId, String deviceId) {
        log.info("退出登录: userId={}, deviceId={}", userId, deviceId);

        // 幂等处理：直接执行退出，无论设备是否存在
        deviceSessionRepository.logoutByUserIdAndDeviceId(userId, deviceId);

        log.info("退出登录成功: userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * 查询用户设备列表
     * 
     * @param userId          当前用户ID
     * @param currentDeviceId 当前设备ID
     * @return 设备列表（包含当前设备和其他设备）
     */
    @Transactional(readOnly = true)
    public DeviceListDTO getDevices(String userId, String currentDeviceId) {
        log.info("查询设备列表: userId={}, currentDeviceId={}", userId, currentDeviceId);

        // 查询用户所有活跃会话
        List<DeviceSession> sessions = deviceSessionRepository.findActiveByUserId(userId);

        DeviceDTO currentDevice = null;
        List<DeviceDTO> otherDevices = new ArrayList<DeviceDTO>();

        for (DeviceSession session : sessions) {
            boolean isCurrent = session.getDeviceId().equals(currentDeviceId);
            DeviceDTO deviceDTO = DeviceDTO.fromDomain(session, isCurrent);

            if (isCurrent) {
                currentDevice = deviceDTO;
            } else {
                otherDevices.add(deviceDTO);
            }
        }

        log.info("查询设备列表成功: userId={}, 当前设备={}, 其他设备数={}",
                userId, currentDevice != null, otherDevices.size());

        return new DeviceListDTO(currentDevice, otherDevices);
    }

    /**
     * 查询用户信息
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    public User getUserProfile(String userId) {
        log.info("查询用户信息: userId={}", userId);

        UserId id = UserId.of(userId);
        Optional<User> userOptional = userRepository.findByUserId(id);
        if (!userOptional.isPresent()) {
            log.warn("查询用户信息失败: 用户不存在, userId={}", userId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return userOptional.get();
    }

    /**
     * 更新用户资料（昵称和/或头像）
     *
     * @param command 更新资料命令
     * @return 更新后的用户实体
     */
    @Transactional
    public User updateProfile(UpdateProfileCommand command) {
        log.info("更新用户资料: userId={}", command.getUserId());

        // 1. 查询用户
        UserId userId = UserId.of(command.getUserId());
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (!userOptional.isPresent()) {
            log.warn("更新资料失败: 用户不存在, userId={}", command.getUserId());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userOptional.get();

        // 2. 修改头像（头像无频率限制）
        if (command.hasAvatar()) {
            user.changeAvatar(command.getAvatar());
            log.info("用户头像已更新: userId={}, avatar={}", command.getUserId(), command.getAvatar());
        }

        // 3. 修改昵称（有频率限制：7天内最多2次）
        if (command.hasNickname()) {
            // 3.1 验证昵称格式和违规词
            Nickname newNickname;
            try {
                newNickname = Nickname.of(command.getNickname());
            } catch (IllegalArgumentException e) {
                String errorMessage = e.getMessage();
                if (errorMessage != null && errorMessage.contains("违规词")) {
                    throw new BusinessException(ErrorCode.USER_NICKNAME_FORBIDDEN);
                }
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }

            // 3.2 执行昵称修改（内含频率限制检查）
            try {
                user.changeNickname(newNickname);
            } catch (IllegalStateException e) {
                log.warn("修改昵称失败: 修改过于频繁, userId={}", command.getUserId());
                throw new BusinessException(ErrorCode.USER_NICKNAME_TOO_FREQUENT);
            }
            log.info("用户昵称已更新: userId={}, nickname={}", command.getUserId(), command.getNickname());
        }

        // 4. 持久化
        userRepository.updateProfile(user);

        log.info("更新用户资料成功: userId={}", command.getUserId());
        return user;
    }

    /**
     * 踢出指定设备
     * 幂等：重复踢出返回成功
     * 
     * @param userId          当前用户ID
     * @param currentDeviceId 当前设备ID（不能踢出自己）
     * @param targetDeviceId  目标设备ID
     */
    @Transactional
    public void logoutDevice(String userId, String currentDeviceId, String targetDeviceId) {
        log.info("踢出设备: userId={}, currentDeviceId={}, targetDeviceId={}",
                userId, currentDeviceId, targetDeviceId);

        // 1. 不能踢出当前设备
        if (currentDeviceId.equals(targetDeviceId)) {
            log.warn("尝试踢出当前设备: deviceId={}", targetDeviceId);
            throw new BusinessException(ErrorCode.DEVICE_CANNOT_LOGOUT_CURRENT);
        }

        // 2. 查询目标设备会话
        Optional<DeviceSession> sessionOptional = deviceSessionRepository.findActiveByUserIdAndDeviceId(userId,
                targetDeviceId);

        // 3. 幂等处理：设备不存在或已退出，直接返回成功
        if (!sessionOptional.isPresent()) {
            log.info("设备已退出或不存在，幂等返回成功: deviceId={}", targetDeviceId);
            return;
        }

        // 4. 执行退出
        deviceSessionRepository.logoutByUserIdAndDeviceId(userId, targetDeviceId);

        log.info("踢出设备成功: userId={}, deviceId={}", userId, targetDeviceId);
    }

    /**
     * 修改密码
     * 修改成功后，其他设备会话将被强制下线
     * 
     * @param command 修改密码命令
     */
    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        log.info("修改密码: userId={}", command.getUserId());

        // 1. 查询用户
        UserId userId = UserId.of(command.getUserId());
        Optional<User> userOptional = userRepository.findByUserId(userId);
        if (!userOptional.isPresent()) {
            log.warn("修改密码失败: 用户不存在, userId={}", command.getUserId());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userOptional.get();

        // 2. 验证旧密码
        boolean passwordMatch = passwordEncoder.matches(command.getOldPassword(), user.getPassword().getHashedValue());
        if (!passwordMatch) {
            log.warn("修改密码失败: 旧密码错误, userId={}", command.getUserId());
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        // 3. 验证新密码格式
        try {
            Password.validatePlainText(command.getNewPassword());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_INVALID);
        }

        // 4. 加密新密码
        String hashedPassword = passwordEncoder.encode(command.getNewPassword());
        Password newPassword = Password.fromHashed(hashedPassword);

        // 5. 更新用户密码
        user.changePassword(newPassword);
        userRepository.updatePassword(user);

        // 6. 使其他设备的会话失效（强制下线），保留当前设备
        List<DeviceSession> sessions = deviceSessionRepository.findActiveByUserId(command.getUserId());
        for (DeviceSession session : sessions) {
            if (!session.getDeviceId().equals(command.getCurrentDeviceId())) {
                deviceSessionRepository.logoutByUserIdAndDeviceId(command.getUserId(), session.getDeviceId());
            }
        }

        log.info("修改密码成功: userId={}, 已下线其他设备数={}",
                command.getUserId(), sessions.size() - 1);
    }

    /**
     * 生成随机密码
     * 包含大小写字母和数字，长度为10位
     */
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(NEW_PASSWORD_LENGTH);
        for (int i = 0; i < NEW_PASSWORD_LENGTH; i++) {
            int index = random.nextInt(PASSWORD_CHARS.length());
            sb.append(PASSWORD_CHARS.charAt(index));
        }
        return sb.toString();
    }
}
