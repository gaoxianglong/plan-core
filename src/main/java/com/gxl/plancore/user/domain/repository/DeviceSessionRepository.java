package com.gxl.plancore.user.domain.repository;

import com.gxl.plancore.user.domain.entity.DeviceSession;

import java.util.List;
import java.util.Optional;

/**
 * 设备会话仓储接口
 * 领域层定义，由基础设施层实现
 */
public interface DeviceSessionRepository {

    /**
     * 保存设备会话
     *
     * @param deviceSession 设备会话实体
     */
    void save(DeviceSession deviceSession);

    /**
     * 根据会话ID查询
     *
     * @param sessionId 会话ID
     * @return 设备会话
     */
    Optional<DeviceSession> findBySessionId(String sessionId);

    /**
     * 根据 access_token 查询
     *
     * @param accessToken JWT access_token
     * @return 设备会话
     */
    Optional<DeviceSession> findByAccessToken(String accessToken);

    /**
     * 根据 refresh_token 查询
     *
     * @param refreshToken JWT refresh_token
     * @return 设备会话
     */
    Optional<DeviceSession> findByRefreshToken(String refreshToken);

    /**
     * 根据用户ID和设备ID查询活跃会话
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     * @return 设备会话
     */
    Optional<DeviceSession> findActiveByUserIdAndDeviceId(String userId, String deviceId);

    /**
     * 查询用户的所有活跃会话
     *
     * @param userId 用户ID
     * @return 活跃会话列表
     */
    List<DeviceSession> findActiveByUserId(String userId);

    /**
     * 统计用户的活跃会话数量
     *
     * @param userId 用户ID
     * @return 活跃会话数量
     */
    int countActiveByUserId(String userId);

    /**
     * 更新设备会话
     *
     * @param deviceSession 设备会话实体
     */
    void update(DeviceSession deviceSession);

    /**
     * 将用户指定设备的会话设为已退出
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     */
    void logoutByUserIdAndDeviceId(String userId, String deviceId);

    /**
     * 将用户的所有会话设为已退出（用于修改密码后强制下线）
     *
     * @param userId 用户ID
     */
    void logoutAllByUserId(String userId);

    /**
     * 删除用户最早的活跃会话（超出设备数限制时使用）
     *
     * @param userId 用户ID
     * @return 被删除的会话，如果没有则返回 empty
     */
    Optional<DeviceSession> deleteOldestActiveSession(String userId);
}
