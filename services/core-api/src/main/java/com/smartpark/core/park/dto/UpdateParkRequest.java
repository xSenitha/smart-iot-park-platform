package com.smartpark.core.park.dto;

import java.math.BigDecimal;

public record UpdateParkRequest(
    String name,
    String location,
    BigDecimal totalAreaSqm,
    String timezone,
    String status
) {}
