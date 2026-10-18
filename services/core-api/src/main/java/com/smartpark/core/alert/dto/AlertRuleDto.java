package com.smartpark.core.alert.dto;

import com.smartpark.core.alert.domain.AlertRule;
import com.smartpark.core.alert.domain.AlertSeverity;
import com.smartpark.core.device.domain.DeviceType;
import java.math.BigDecimal;
import java.time.Instant;

public record AlertRuleDto(
    Long id,
    String name,
    DeviceType deviceType,
    String metricName,
    String operator,
    BigDecimal thresholdValue,
    AlertSeverity severity,
    boolean isActive,
    Instant createdAt
) {
    public static AlertRuleDto fromEntity(AlertRule rule) {
        return new AlertRuleDto(
            rule.getId(),
            rule.getName(),
            rule.getDeviceType(),
            rule.getMetricName(),
            rule.getOperator(),
            rule.getThresholdValue(),
            rule.getSeverity(),
            rule.isActive(),
            rule.getCreatedAt()
        );
    }
}
