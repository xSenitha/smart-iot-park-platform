package com.smartpark.core.zone.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateZoneRequest(
    @NotBlank(message = "Zone name is required")
    String name,

    @NotBlank(message = "Zone type is required (e.g. PLAZA, BOTANICAL, SPORTS_LAWN)")
    String zoneType,

    String polygonGeojson,
    BigDecimal targetMoistureLevel
) {}
