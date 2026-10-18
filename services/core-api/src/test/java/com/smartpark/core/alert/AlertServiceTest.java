package com.smartpark.core.alert;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartpark.core.alert.domain.Alert;
import com.smartpark.core.alert.domain.AlertSeverity;
import com.smartpark.core.alert.domain.AlertStatus;
import com.smartpark.core.alert.dto.AlertDto;
import com.smartpark.core.alert.dto.CreateAlertRequest;
import com.smartpark.core.alert.dto.ResolveAlertRequest;
import com.smartpark.core.alert.repository.AlertRepository;
import com.smartpark.core.alert.repository.AlertRuleRepository;
import com.smartpark.core.alert.service.AlertService;
import com.smartpark.core.audit.service.AuditService;
import com.smartpark.core.auth.repository.UserRepository;
import com.smartpark.core.device.domain.Device;
import com.smartpark.core.device.domain.DeviceType;
import com.smartpark.core.device.repository.DeviceRepository;
import com.smartpark.core.event.SseEventBroadcaster;
import com.smartpark.core.park.domain.Park;
import com.smartpark.core.zone.domain.Zone;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private SseEventBroadcaster eventBroadcaster;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(alertRepository, alertRuleRepository, deviceRepository, userRepository, auditService, eventBroadcaster);
    }

    @Test
    void testCreateAlert_Success() {
        Park park = new Park("Central Park", "Downtown", new BigDecimal("500000"), "UTC", "ACTIVE");
        park.setId(1L);
        Zone zone = new Zone(park, "Central Plaza", "PLAZA", null, new BigDecimal("40.00"));
        zone.setId(10L);
        Device device = new Device(zone, "DEV-BIN-0001", "Bin 1", DeviceType.BIN, "X1", "v1.0");
        device.setId(5L);

        when(deviceRepository.findById(5L)).thenReturn(Optional.of(device));

        Alert alert = new Alert(device, null, "OVERFLOW", AlertSeverity.CRITICAL, "Bin Full", "Bin is 95% full");
        alert.setId(100L);
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);

        CreateAlertRequest request = new CreateAlertRequest(5L, null, "OVERFLOW", AlertSeverity.CRITICAL, "Bin Full", "Bin is 95% full");
        AlertDto result = alertService.createAlert(request);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.severity()).isEqualTo(AlertSeverity.CRITICAL);
        verify(eventBroadcaster).broadcast(eq("ALERT_TRIGGERED"), any());
    }

    @Test
    void testResolveAlert() {
        Park park = new Park("Central Park", "Downtown", new BigDecimal("500000"), "UTC", "ACTIVE");
        park.setId(1L);
        Zone zone = new Zone(park, "Central Plaza", "PLAZA", null, new BigDecimal("40.00"));
        zone.setId(10L);
        Device device = new Device(zone, "DEV-BIN-0001", "Bin 1", DeviceType.BIN, "X1", "v1.0");
        device.setId(5L);

        Alert alert = new Alert(device, null, "OVERFLOW", AlertSeverity.CRITICAL, "Bin Full", "Bin is 95% full");
        alert.setId(100L);
        when(alertRepository.findById(100L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);

        ResolveAlertRequest request = new ResolveAlertRequest("Bin emptied by maintenance crew");
        AlertDto result = alertService.resolveAlert(100L, request);

        assertThat(result.status()).isEqualTo(AlertStatus.RESOLVED);
        verify(eventBroadcaster).broadcast(eq("ALERT_RESOLVED"), any());
    }
}
