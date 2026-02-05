package com.gxl.plancore.user.application.dto;

import com.gxl.plancore.user.domain.entity.DeviceSession;

import java.time.Instant;

/**
 * 设备信息 DTO
 */
public class DeviceDTO {

    private final String deviceId;
    private final String deviceName;
    private final String platform;
    private final String lastLoginIp;
    private final Instant lastLoginAt;
    private final boolean isCurrent;

    public DeviceDTO(String deviceId, String deviceName, String platform,
                     String lastLoginIp, Instant lastLoginAt, boolean isCurrent) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.platform = platform;
        this.lastLoginIp = lastLoginIp;
        this.lastLoginAt = lastLoginAt;
        this.isCurrent = isCurrent;
    }

    /**
     * 从领域实体创建 DTO
     */
    public static DeviceDTO fromDomain(DeviceSession session, boolean isCurrent) {
        return new DeviceDTO(
                session.getDeviceId(),
                session.getDeviceName(),
                session.getPlatform(),
                session.getLastLoginIp(),
                session.getLastLoginAt(),
                isCurrent
        );
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getPlatform() {
        return platform;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public boolean isCurrent() {
        return isCurrent;
    }
}
