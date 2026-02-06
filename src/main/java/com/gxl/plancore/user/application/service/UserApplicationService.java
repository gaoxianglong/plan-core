package com.gxl.plancore.user.application.service;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.user.application.command.UpdateProfileCommand;
import com.gxl.plancore.user.domain.entity.User;
import com.gxl.plancore.user.domain.repository.UserRepository;
import com.gxl.plancore.user.domain.valueobject.Nickname;
import com.gxl.plancore.user.domain.valueobject.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户应用服务
 * 编排用户中心相关业务流程（资料修改等）
 */
@Service
public class UserApplicationService {

    private static final Logger log = LoggerFactory.getLogger(UserApplicationService.class);

    private final UserRepository userRepository;

    public UserApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
