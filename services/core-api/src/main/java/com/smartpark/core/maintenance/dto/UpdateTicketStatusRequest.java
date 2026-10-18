package com.smartpark.core.maintenance.dto;

import com.smartpark.core.maintenance.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketStatusRequest(
    @NotNull(message = "Ticket status is required (PENDING, IN_PROGRESS, RESOLVED, CLOSED)")
    TicketStatus status
) {}
