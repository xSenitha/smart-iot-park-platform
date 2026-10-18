package com.smartpark.core.zone.dto;

import com.smartpark.core.zone.domain.Zone;
import java.math.BigDecimal;
import java.time.Instant;

public record ZoneDto(
    Long id,
    Long parkId,
    String parkName,
    String name,
    String zoneType,
    String polygonGeojson,
    BigDecimal targetMoistureLevel,
    int deviceCount,
    Instant createdAt,
    Instant updatedAt
) {
    public static ZoneDto fromEntity(Zone zone) {
        int count = zone.getDevices() != null ? zone.getDevices().size() : 0;
        return new ZoneDto(
            zone.getId(),
            zone.getPark().getId(),
            zone.getPark().getName(),
            zone.getName(),
            zone.getZoneType(),
            zone.getPolygonGeojson(),
            zone.getTargetMoistureLevel(),
            count,
            zone.getCreatedAt(),
            zone.getUpdatedAt()
        );
    }
}
