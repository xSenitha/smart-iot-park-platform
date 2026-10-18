package com.smartpark.core.device.dto;

import com.smartpark.core.device.domain.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDeviceRequest(
    @NotNull(message = "Zone ID is required")
    Long zoneId,

    @NotBlank(message = "Device UID is required (e.g. DEV-BIN-0001)")
    String deviceUid,

    @NotBlank(message = "Device name is required")
    String name,

    @NotNull(message = "Device type is required (BIN, SOIL, LIGHT)")
    DeviceType type,

    String model,
    String firmwareVersion
) {}
