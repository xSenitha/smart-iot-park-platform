package com.smartpark.core.maintenance;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartpark.core.audit.service.AuditService;
import com.smartpark.core.auth.domain.User;
import com.smartpark.core.auth.repository.UserRepository;
import com.smartpark.core.device.domain.Device;
import com.smartpark.core.device.domain.DeviceType;
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
import com.smartpark.core.maintenance.service.MaintenanceService;
import com.smartpark.core.park.domain.Park;
import com.smartpark.core.park.repository.ParkRepository;
import com.smartpark.core.zone.domain.Zone;
import com.smartpark.core.zone.repository.ZoneRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceTicketRepository ticketRepository;

    @Mock
    private ParkRepository parkRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private SseEventBroadcaster eventBroadcaster;

    private MaintenanceService maintenanceService;

    @BeforeEach
    void setUp() {
        maintenanceService = new MaintenanceService(ticketRepository, parkRepository, zoneRepository, deviceRepository, userRepository, auditService, eventBroadcaster);
    }

    @Test
    void testCreateTicket_Success() {
        Park park = new Park("Central Park", "Downtown", new BigDecimal("500000"), "UTC", "ACTIVE");
        park.setId(1L);
        Zone zone = new Zone(park, "Central Plaza", "PLAZA", null, new BigDecimal("40.00"));
        zone.setId(10L);
        Device device = new Device(zone, "DEV-LIGHT-0001", "Pole 1", DeviceType.LIGHT, "L1", "v1.0");
        device.setId(15L);

        when(parkRepository.findById(1L)).thenReturn(Optional.of(park));
        when(zoneRepository.findById(10L)).thenReturn(Optional.of(zone));
        when(deviceRepository.findById(15L)).thenReturn(Optional.of(device));

        MaintenanceTicket ticket = new MaintenanceTicket(park, zone, device, "Bulb Outage", "Replace bulb", TicketPriority.HIGH, "MANUAL");
        ticket.setId(50L);
        when(ticketRepository.save(any(MaintenanceTicket.class))).thenReturn(ticket);

        CreateTicketRequest request = new CreateTicketRequest(1L, 10L, 15L, "Bulb Outage", "Replace bulb", TicketPriority.HIGH, null, "MANUAL");
        MaintenanceTicketDto result = maintenanceService.createTicket(request);

        assertThat(result.id()).isEqualTo(50L);
        assertThat(result.priority()).isEqualTo(TicketPriority.HIGH);
        verify(eventBroadcaster).broadcast(eq("TICKET_CREATED"), any());
    }

    @Test
    void testAssignTicket_ProgressesToInProgress() {
        Park park = new Park("Central Park", "Downtown", new BigDecimal("500000"), "UTC", "ACTIVE");
        park.setId(1L);
        Zone zone = new Zone(park, "Central Plaza", "PLAZA", null, new BigDecimal("40.00"));
        zone.setId(10L);

        MaintenanceTicket ticket = new MaintenanceTicket(park, zone, null, "Inspect Bench", "Check bench damage", TicketPriority.LOW, "MANUAL");
        ticket.setId(50L);
        ticket.setStatus(TicketStatus.PENDING);

        User tech = new User("tech_alex", "alex@smartpark.local", "hash", "Alex Tech");
        tech.setId(3L);

        when(ticketRepository.findById(50L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(tech));
        when(ticketRepository.save(any(MaintenanceTicket.class))).thenReturn(ticket);

        AssignTicketRequest request = new AssignTicketRequest(3L);
        MaintenanceTicketDto result = maintenanceService.assignTicket(50L, request);

        assertThat(result.status()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(ticket.getAssignedTo()).isEqualTo(tech);
        verify(eventBroadcaster).broadcast(eq("TICKET_ASSIGNED"), any());
    }
}
