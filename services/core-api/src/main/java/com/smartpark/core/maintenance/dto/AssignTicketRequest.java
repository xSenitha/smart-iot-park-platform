package com.smartpark.core.maintenance.dto;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequest(
    @NotNull(message = "Assigned user ID is required")
    Long assignedToId
) {}
