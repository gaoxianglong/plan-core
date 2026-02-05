package com.gxl.plancore.user.interfaces.dto;

import java.util.List;

/**
 * 设备列表响应 DTO
 */
public class DeviceListResponse {

    private DeviceResponse currentDevice;
    private List<DeviceResponse> otherDevices;

    public DeviceListResponse() {
    }

    public DeviceListResponse(DeviceResponse currentDevice, List<DeviceResponse> otherDevices) {
        this.currentDevice = currentDevice;
        this.otherDevices = otherDevices;
    }

    public DeviceResponse getCurrentDevice() {
        return currentDevice;
    }

    public void setCurrentDevice(DeviceResponse currentDevice) {
        this.currentDevice = currentDevice;
    }

    public List<DeviceResponse> getOtherDevices() {
        return otherDevices;
    }

    public void setOtherDevices(List<DeviceResponse> otherDevices) {
        this.otherDevices = otherDevices;
    }
}
