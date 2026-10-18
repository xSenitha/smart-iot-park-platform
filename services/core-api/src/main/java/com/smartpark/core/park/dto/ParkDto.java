package com.smartpark.core.park.dto;

import com.smartpark.core.park.domain.Park;
import java.math.BigDecimal;
import java.time.Instant;

public record ParkDto(
    Long id,
    String name,
    String location,
    BigDecimal totalAreaSqm,
    String timezone,
    String status,
    int totalZones,
    Instant createdAt,
    Instant updatedAt
) {
    public static ParkDto fromEntity(Park park) {
        int zonesCount = park.getZones() != null ? park.getZones().size() : 0;
        return new ParkDto(
            park.getId(),
            park.getName(),
            park.getLocation(),
            park.getTotalAreaSqm(),
            park.getTimezone(),
            park.getStatus(),
            zonesCount,
            park.getCreatedAt(),
            park.getUpdatedAt()
        );
    }
}
