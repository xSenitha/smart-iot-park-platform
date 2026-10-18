package com.smartpark.core.maintenance.service;

import com.smartpark.core.audit.service.AuditService;
import com.smartpark.core.auth.domain.User;
import com.smartpark.core.auth.repository.UserRepository;
import com.smartpark.core.common.dto.PageResponse;
import com.smartpark.core.common.exception.ResourceNotFoundException;
import com.smartpark.core.device.domain.Device;
import com.smartpark.core.device.repository.DeviceRepository;
import com.smartpark.core.event.SseEventBroadcaster;
import com.smartpark.core.maintenance.domain.MaintenanceTicket;
import com.smartpark.core.maintenance.domain.TicketPriority;
import com.smartpark.core.maintenance.domain.TicketStatus;
import com.smartpark.core.maintenance.dto.AssignTicketRequest;
import com.smartpark.core.maintenance.dto.CreateTicketRequest;
import com.smartpark.core.maintenance.dto.MaintenanceTicketDto;
import com.smartpark.core.maintenance.dto.UpdateTicketStatusRequest;
import com.smartpark.core.maintenance.repository.MaintenanceTicketRepository;
import com.smartpark.core.park.domain.Park;
import com.smartpark.core.park.repository.ParkRepository;
import com.smartpark.core.zone.domain.Zone;
import com.smartpark.core.zone.repository.ZoneRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceService {

    private final MaintenanceTicketRepository ticketRepository;
    private final ParkRepository parkRepository;
    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SseEventBroadcaster eventBroadcaster;

    public MaintenanceService(
        MaintenanceTicketRepository ticketRepository,
        ParkRepository parkRepository,
        ZoneRepository zoneRepository,
        DeviceRepository deviceRepository,
        UserRepository userRepository,
        AuditService auditService,
        SseEventBroadcaster eventBroadcaster
    ) {
        this.ticketRepository = ticketRepository;
        this.parkRepository = parkRepository;
        this.zoneRepository = zoneRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.eventBroadcaster = eventBroadcaster;
    }

    public PageResponse<MaintenanceTicketDto> getTickets(TicketStatus status, TicketPriority priority, Pageable pageable) {
        Page<MaintenanceTicket> page;
        if (status != null) {
            page = ticketRepository.findByStatus(status, pageable);
        } else if (priority != null) {
            page = ticketRepository.findByPriority(priority, pageable);
        } else {
            page = ticketRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(MaintenanceTicketDto::fromEntity));
    }

    public MaintenanceTicketDto getTicketById(Long id) {
        MaintenanceTicket ticket = findTicketEntity(id);
        return MaintenanceTicketDto.fromEntity(ticket);
    }

    @Transactional
    public MaintenanceTicketDto createTicket(CreateTicketRequest request) {
        Park park = parkRepository.findById(request.parkId())
            .orElseThrow(() -> new ResourceNotFoundException("Park", "id", request.parkId()));

        Zone zone = zoneRepository.findById(request.zoneId())
            .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", request.zoneId()));

        Device device = null;
        if (request.deviceId() != null) {
            device = deviceRepository.findById(request.deviceId()).orElse(null);
        }

        MaintenanceTicket ticket = new MaintenanceTicket(
            park,
            zone,
            device,
            request.title(),
            request.description(),
            request.priority(),
            request.source()
        );

        if (request.assignedToId() != null) {
            User assignee = userRepository.findById(request.assignedToId()).orElse(null);
            ticket.setAssignedTo(assignee);
        }

        MaintenanceTicket saved = ticketRepository.save(ticket);
        MaintenanceTicketDto dto = MaintenanceTicketDto.fromEntity(saved);

        eventBroadcaster.broadcast("TICKET_CREATED", dto);
        auditService.logAction("CREATE", "TICKET", saved.getId(), "Created maintenance ticket: " + saved.getTitle());
        return dto;
    }

    @Transactional
    public MaintenanceTicketDto updateTicketStatus(Long id, UpdateTicketStatusRequest request) {
        MaintenanceTicket ticket = findTicketEntity(id);
        ticket.setStatus(request.status());

        if (request.status() == TicketStatus.RESOLVED || request.status() == TicketStatus.CLOSED) {
            ticket.setResolvedAt(Instant.now());
        }

        MaintenanceTicket updated = ticketRepository.save(ticket);
        MaintenanceTicketDto dto = MaintenanceTicketDto.fromEntity(updated);

        eventBroadcaster.broadcast("TICKET_STATUS_CHANGED", dto);
        auditService.logAction("UPDATE_STATUS", "TICKET", updated.getId(), "Ticket status changed to: " + request.status());
        return dto;
    }

    @Transactional
    public MaintenanceTicketDto assignTicket(Long id, AssignTicketRequest request) {
        MaintenanceTicket ticket = findTicketEntity(id);
        User assignee = userRepository.findById(request.assignedToId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assignedToId()));

        ticket.setAssignedTo(assignee);
        if (ticket.getStatus() == TicketStatus.PENDING) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        MaintenanceTicket updated = ticketRepository.save(ticket);
        MaintenanceTicketDto dto = MaintenanceTicketDto.fromEntity(updated);

        eventBroadcaster.broadcast("TICKET_ASSIGNED", dto);
        auditService.logAction("ASSIGN", "TICKET", updated.getId(), "Assigned ticket to: " + assignee.getUsername());
        return dto;
    }

    public MaintenanceTicket findTicketEntity(Long id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MaintenanceTicket", "id", id));
    }
}
