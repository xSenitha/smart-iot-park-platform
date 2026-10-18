package com.smartpark.ingestion.common.dto;

import java.time.Instant;

public record TelemetryStatsDto(
    long totalReadingsCount,
    long binReadingsCount,
    long soilReadingsCount,
    long lightReadingsCount,
    Instant latestReadingTimestamp
) {}
