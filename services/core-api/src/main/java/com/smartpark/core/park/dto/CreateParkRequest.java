package com.smartpark.core.park.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateParkRequest(
    @NotBlank(message = "Park name is required")
    String name,

    @NotBlank(message = "Park location is required")
    String location,

    @NotNull(message = "Total area in square meters is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total area must be greater than zero")
    BigDecimal totalAreaSqm,

    String timezone,
    String status
) {}
