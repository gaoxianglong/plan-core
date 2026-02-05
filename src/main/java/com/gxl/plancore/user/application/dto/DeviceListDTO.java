package com.gxl.plancore.user.application.dto;

import java.util.List;

/**
 * 设备列表 DTO
 */
public class DeviceListDTO {

    private final DeviceDTO currentDevice;
    private final List<DeviceDTO> otherDevices;

    public DeviceListDTO(DeviceDTO currentDevice, List<DeviceDTO> otherDevices) {
        this.currentDevice = currentDevice;
        this.otherDevices = otherDevices;
    }

    public DeviceDTO getCurrentDevice() {
        return currentDevice;
    }

    public List<DeviceDTO> getOtherDevices() {
        return otherDevices;
    }
}
