package com.smartpark.core.alert.dto;

import com.smartpark.core.alert.domain.Alert;
import com.smartpark.core.alert.domain.AlertSeverity;
import com.smartpark.core.alert.domain.AlertStatus;
import java.time.Instant;

public record AlertDto(
    Long id,
    Long deviceId,
    String deviceUid,
    String deviceName,
    Long zoneId,
    String zoneName,
    Long parkId,
    String alertType,
    AlertSeverity severity,
    String title,
    String message,
    AlertStatus status,
    Instant triggeredAt,
    Instant resolvedAt,
    String resolvedByUsername,
    String resolutionNotes
) {
    public static AlertDto fromEntity(Alert alert) {
        String resolvedBy = alert.getResolvedBy() != null ? alert.getResolvedBy().getUsername() : null;
        return new AlertDto(
            alert.getId(),
            alert.getDevice().getId(),
            alert.getDevice().getDeviceUid(),
            alert.getDevice().getName(),
            alert.getDevice().getZone().getId(),
            alert.getDevice().getZone().getName(),
            alert.getDevice().getZone().getPark().getId(),
            alert.getAlertType(),
            alert.getSeverity(),
            alert.getTitle(),
            alert.getMessage(),
            alert.getStatus(),
            alert.getTriggeredAt(),
            alert.getResolvedAt(),
            resolvedBy,
            alert.getResolutionNotes()
        );
    }
}
