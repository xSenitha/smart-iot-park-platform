package com.smartpark.core.alert.dto;

import com.smartpark.core.alert.domain.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAlertRequest(
    @NotNull(message = "Device ID is required")
    Long deviceId,

    Long ruleId,

    @NotBlank(message = "Alert type is required")
    String alertType,

    @NotNull(message = "Severity is required (INFO, WARNING, CRITICAL)")
    AlertSeverity severity,

    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Message is required")
    String message
) {}
