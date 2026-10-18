package com.smartpark.core.device.dto;

import com.smartpark.core.device.domain.DeviceStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateDeviceStatusRequest(
    @NotNull(message = "Device status is required (ACTIVE, INACTIVE, FAULT, MAINTENANCE)")
    DeviceStatus status,

    BigDecimal batteryLevel
) {}
