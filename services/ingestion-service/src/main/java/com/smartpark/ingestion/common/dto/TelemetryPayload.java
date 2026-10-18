package com.smartpark.ingestion.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

public record TelemetryPayload(
    @NotBlank(message = "deviceId is required")
    String deviceId,

    @NotBlank(message = "deviceType is required (BIN, SOIL, LIGHT)")
    String deviceType,

    @NotNull(message = "zoneId is required")
    Long zoneId,

    @NotNull(message = "parkId is required")
    Long parkId,

    Instant timestamp,

    @NotNull(message = "metrics object is required")
    Map<String, Object> metrics,

    Map<String, Object> metadata
) {}
