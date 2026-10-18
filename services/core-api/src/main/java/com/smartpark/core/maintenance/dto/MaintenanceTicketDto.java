package com.smartpark.core.maintenance.dto;

import com.smartpark.core.maintenance.domain.MaintenanceTicket;
import com.smartpark.core.maintenance.domain.TicketPriority;
import com.smartpark.core.maintenance.domain.TicketStatus;
import java.time.Instant;

public record MaintenanceTicketDto(
    Long id,
    Long parkId,
    String parkName,
    Long zoneId,
    String zoneName,
    Long deviceId,
    String deviceUid,
    String deviceName,
    String title,
    String description,
    TicketPriority priority,
    TicketStatus status,
    Long assignedToId,
    String assignedToName,
    String source,
    Instant createdAt,
    Instant updatedAt,
    Instant resolvedAt
) {
    public static MaintenanceTicketDto fromEntity(MaintenanceTicket ticket) {
        Long deviceId = ticket.getDevice() != null ? ticket.getDevice().getId() : null;
        String deviceUid = ticket.getDevice() != null ? ticket.getDevice().getDeviceUid() : null;
        String deviceName = ticket.getDevice() != null ? ticket.getDevice().getName() : null;

        Long assigneeId = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null;
        String assigneeName = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFullName() : null;

        return new MaintenanceTicketDto(
            ticket.getId(),
            ticket.getPark().getId(),
            ticket.getPark().getName(),
            ticket.getZone().getId(),
            ticket.getZone().getName(),
            deviceId,
            deviceUid,
            deviceName,
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getPriority(),
            ticket.getStatus(),
            assigneeId,
            assigneeName,
            ticket.getSource(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt(),
            ticket.getResolvedAt()
        );
    }
}
