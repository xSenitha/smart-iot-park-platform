package com.smartpark.core.device.dto;

import com.smartpark.core.device.domain.Device;
import com.smartpark.core.device.domain.DeviceStatus;
import com.smartpark.core.device.domain.DeviceType;
import java.math.BigDecimal;
import java.time.Instant;

public record DeviceDto(
    Long id,
    Long zoneId,
    String zoneName,
    Long parkId,
    String parkName,
    String deviceUid,
    String name,
    DeviceType type,
    String model,
    String firmwareVersion,
    BigDecimal batteryLevel,
    DeviceStatus status,
    Instant lastHeartbeat,
    Instant createdAt,
    Instant updatedAt
) {
    public static DeviceDto fromEntity(Device device) {
        return new DeviceDto(
            device.getId(),
            device.getZone().getId(),
            device.getZone().getName(),
            device.getZone().getPark().getId(),
            device.getZone().getPark().getName(),
            device.getDeviceUid(),
            device.getName(),
            device.getType(),
            device.getModel(),
            device.getFirmwareVersion(),
            device.getBatteryLevel(),
            device.getStatus(),
            device.getLastHeartbeat(),
            device.getCreatedAt(),
            device.getUpdatedAt()
        );
    }
}
