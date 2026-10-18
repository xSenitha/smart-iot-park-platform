package com.smartpark.core.maintenance.dto;

import com.smartpark.core.maintenance.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTicketRequest(
    @NotNull(message = "Park ID is required")
    Long parkId,

    @NotNull(message = "Zone ID is required")
    Long zoneId,

    Long deviceId,

    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Description is required")
    String description,

    TicketPriority priority,
    Long assignedToId,
    String source
) {}
