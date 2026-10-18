package com.smartpark.core.zone.dto;

import java.math.BigDecimal;

public record ZoneHealthScoreDto(
    Long zoneId,
    String zoneName,
    int healthScorePercentage, // 0 - 100
    int totalDevices,
    int activeDevices,
    int faultDevices,
    int openAlerts,
    String statusVerdict // EXCELLENT, GOOD, ATTENTION_REQUIRED, CRITICAL
) {}
