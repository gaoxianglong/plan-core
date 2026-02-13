package com.gxl.plancore.user.infrastructure.persistence.converter;

import com.gxl.plancore.user.domain.entity.DeviceSession;
import com.gxl.plancore.user.infrastructure.persistence.po.DeviceSessionPO;

/**
 * DeviceSession 领域对象与持久化对象转换器
 */
public class DeviceSessionConverter {

    /**
     * PO -> 领域对象
     */
    public static DeviceSession toEntity(DeviceSessionPO po) {
        if (po == null) {
            return null;
        }
        return DeviceSession.reconstitute(
                po.getSessionId(),
                po.getUserId(),
                po.getDeviceId(),
                po.getDeviceName(),
                po.getPlatform(),
                po.getOsVersion(),
                po.getAppVersion(),
                po.getAccessToken(),
                po.getRefreshToken(),
                po.getExpiresAt(),
                po.getRefreshExpiresAt(),
                po.getLastLoginIp(),
                po.getLastLoginAt(),
                po.getLastActiveAt(),
                DeviceSession.Status.valueOf(po.getStatus()),
                po.getLoggedOutAt(),
                po.getCreatedAt(),
                po.getUpdatedAt());
    }

    /**
     * 领域对象 -> PO
     */
    public static DeviceSessionPO toPO(DeviceSession session) {
        if (session == null) {
            return null;
        }
        DeviceSessionPO po = new DeviceSessionPO();
        po.setSessionId(session.getSessionId());
        po.setUserId(session.getUserId());
        po.setDeviceId(session.getDeviceId());
        po.setDeviceName(session.getDeviceName());
        po.setPlatform(session.getPlatform());
        po.setOsVersion(session.getOsVersion());
        po.setAppVersion(session.getAppVersion());
        po.setAccessToken(session.getAccessToken());
        po.setRefreshToken(session.getRefreshToken());
        po.setExpiresAt(session.getExpiresAt());
        po.setRefreshExpiresAt(session.getRefreshExpiresAt());
        po.setLastLoginIp(session.getLastLoginIp());
        po.setLastLoginAt(session.getLastLoginAt());
        po.setLastActiveAt(session.getLastActiveAt());
        po.setStatus(session.getStatus().name());
        po.setLoggedOutAt(session.getLoggedOutAt());
        po.setCreatedAt(session.getCreatedAt());
        po.setUpdatedAt(session.getUpdatedAt());
        return po;
    }
}
