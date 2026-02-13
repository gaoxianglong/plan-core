package com.gxl.plancore.user.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gxl.plancore.user.domain.entity.DeviceSession;
import com.gxl.plancore.user.domain.repository.DeviceSessionRepository;
import com.gxl.plancore.user.infrastructure.persistence.converter.DeviceSessionConverter;
import com.gxl.plancore.user.infrastructure.persistence.mapper.DeviceSessionMapper;
import com.gxl.plancore.user.infrastructure.persistence.po.DeviceSessionPO;

/**
 * 设备会话仓储实现
 */
@Repository
public class DeviceSessionRepositoryImpl implements DeviceSessionRepository {

    private final DeviceSessionMapper deviceSessionMapper;

    public DeviceSessionRepositoryImpl(DeviceSessionMapper deviceSessionMapper) {
        this.deviceSessionMapper = deviceSessionMapper;
    }

    @Override
    public void save(DeviceSession deviceSession) {
        DeviceSessionPO po = DeviceSessionConverter.toPO(deviceSession);
        deviceSessionMapper.insert(po);
    }

    @Override
    public Optional<DeviceSession> findBySessionId(String sessionId) {
        DeviceSessionPO po = deviceSessionMapper.selectBySessionId(sessionId);
        return Optional.ofNullable(DeviceSessionConverter.toEntity(po));
    }

    @Override
    public Optional<DeviceSession> findByAccessToken(String accessToken) {
        DeviceSessionPO po = deviceSessionMapper.selectByAccessToken(accessToken);
        return Optional.ofNullable(DeviceSessionConverter.toEntity(po));
    }

    @Override
    public Optional<DeviceSession> findByRefreshToken(String refreshToken) {
        DeviceSessionPO po = deviceSessionMapper.findByRefreshToken(refreshToken);
        return Optional.ofNullable(DeviceSessionConverter.toEntity(po));
    }

    @Override
    public Optional<DeviceSession> findActiveByUserIdAndDeviceId(String userId, String deviceId) {
        DeviceSessionPO po = deviceSessionMapper.selectActiveByUserIdAndDeviceId(userId, deviceId);
        return Optional.ofNullable(DeviceSessionConverter.toEntity(po));
    }

    @Override
    public List<DeviceSession> findActiveByUserId(String userId) {
        List<DeviceSessionPO> poList = deviceSessionMapper.selectActiveByUserId(userId);
        List<DeviceSession> result = new ArrayList<>();
        for (DeviceSessionPO po : poList) {
            result.add(DeviceSessionConverter.toEntity(po));
        }
        return result;
    }

    @Override
    public int countActiveByUserId(String userId) {
        return deviceSessionMapper.countActiveByUserId(userId);
    }

    @Override
    public void update(DeviceSession deviceSession) {
        DeviceSessionPO po = DeviceSessionConverter.toPO(deviceSession);
        deviceSessionMapper.update(po);
    }

    @Override
    public void logoutByUserIdAndDeviceId(String userId, String deviceId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        deviceSessionMapper.logoutByUserIdAndDeviceId(userId, deviceId, now, now);
    }

    @Override
    public void logoutAllByUserId(String userId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        deviceSessionMapper.logoutAllByUserId(userId, now, now);
    }

    @Override
    public Optional<DeviceSession> deleteOldestActiveSession(String userId) {
        DeviceSessionPO oldest = deviceSessionMapper.selectOldestActiveSession(userId);
        if (oldest == null) {
            return Optional.empty();
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        deviceSessionMapper.logoutBySessionId(oldest.getSessionId(), now, now);
        return Optional.ofNullable(DeviceSessionConverter.toEntity(oldest));
    }
}
