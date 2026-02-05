package com.gxl.plancore.user.infrastructure.persistence.converter;

import com.gxl.plancore.user.domain.entity.DeviceSession;
import com.gxl.plancore.user.infrastructure.persistence.po.DeviceSessionPO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * DeviceSession 领域实体与 PO 之间的转换器
 */
public class DeviceSessionConverter {

    private DeviceSessionConverter() {
    }

    /**
     * 领域实体转 PO
     */
    public static DeviceSessionPO toPO(DeviceSession entity) {
        if (entity == null) {
            return null;
        }
        DeviceSessionPO po = new DeviceSessionPO();
        po.setSessionId(entity.getSessionId());
        po.setUserId(entity.getUserId());
        po.setDeviceId(entity.getDeviceId());
        po.setDeviceName(entity.getDeviceName());
        po.setPlatform(entity.getPlatform());
        po.setOsVersion(entity.getOsVersion());
        po.setAppVersion(entity.getAppVersion());
        po.setAccessToken(entity.getAccessToken());
        po.setRefreshToken(entity.getRefreshToken());
        po.setExpiresAt(toLocalDateTime(entity.getExpiresAt()));
        po.setRefreshExpiresAt(toLocalDateTime(entity.getRefreshExpiresAt()));
        po.setLastLoginIp(entity.getLastLoginIp());
        po.setLastLoginAt(toLocalDateTime(entity.getLastLoginAt()));
        po.setLastActiveAt(toLocalDateTime(entity.getLastActiveAt()));
        po.setStatus(entity.getStatus().name());
        po.setLoggedOutAt(toLocalDateTime(entity.getLoggedOutAt()));
        po.setCreatedAt(toLocalDateTime(entity.getCreatedAt()));
        po.setUpdatedAt(toLocalDateTime(entity.getUpdatedAt()));
        return po;
    }

    /**
     * PO 转领域实体
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
                toInstant(po.getExpiresAt()),
                toInstant(po.getRefreshExpiresAt()),
                po.getLastLoginIp(),
                toInstant(po.getLastLoginAt()),
                toInstant(po.getLastActiveAt()),
                DeviceSession.Status.valueOf(po.getStatus()),
                toInstant(po.getLoggedOutAt()),
                toInstant(po.getCreatedAt()),
                toInstant(po.getUpdatedAt())
        );
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toInstant(ZoneOffset.UTC);
    }
}
