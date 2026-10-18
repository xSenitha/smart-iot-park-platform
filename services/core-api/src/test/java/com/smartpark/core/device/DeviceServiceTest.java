package com.smartpark.core.device;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartpark.core.audit.service.AuditService;
import com.smartpark.core.device.domain.Device;
import com.smartpark.core.device.domain.DeviceStatus;
import com.smartpark.core.device.domain.DeviceType;
import com.smartpark.core.device.dto.CreateDeviceRequest;
import com.smartpark.core.device.dto.DeviceDto;
import com.smartpark.core.device.dto.UpdateDeviceStatusRequest;
import com.smartpark.core.device.repository.DeviceRepository;
import com.smartpark.core.device.service.DeviceService;
import com.smartpark.core.event.SseEventBroadcaster;
import com.smartpark.core.park.domain.Park;
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
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private SseEventBroadcaster eventBroadcaster;

    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceService(deviceRepository, zoneRepository, auditService, eventBroadcaster);
    }

    @Test
    void testCreateDevice_Success() {
        Park park = new Park("Central Park", "Downtown", new BigDecimal("500000"), "UTC", "ACTIVE");
        park.setId(1L);

        Zone zone = new Zone(park, "Central Plaza", "PLAZA", null, new BigDecimal("40.00"));
        zone.setId(10L);

        CreateDeviceRequest request = new CreateDeviceRequest(10L, "DEV-BIN-9999", "New Bin", DeviceType.BIN, "X1", "v2.0");

        when(deviceRepository.findByDeviceUid("DEV-BIN-9999")).thenReturn(Optional.empty());
        when(zoneRepository.findById(10L)).thenReturn(Optional.of(zone));

        Device saved = new Device(zone, "DEV-BIN-9999", "New Bin", DeviceType.BIN, "X1", "v2.0");
        saved.setId(100L);
        when(deviceRepository.save(any(Device.class))).thenReturn(saved);

        DeviceDto result = deviceService.createDevice(request);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.deviceUid()).isEqualTo("DEV-BIN-9999");
        verify(eventBroadcaster).broadcast(eq("DEVICE_REGISTERED"), any());
    }

    @Test
    void testUpdateDeviceStatus() {
        Park park = new Park("Central Park", "Downtown", new BigDecimal("500000"), "UTC", "ACTIVE");
        park.setId(1L);
        Zone zone = new Zone(park, "Central Plaza", "PLAZA", null, new BigDecimal("40.00"));
        zone.setId(10L);

        Device device = new Device(zone, "DEV-BIN-0001", "Bin 1", DeviceType.BIN, "X1", "v1.0");
        device.setId(1L);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenReturn(device);

        UpdateDeviceStatusRequest request = new UpdateDeviceStatusRequest(DeviceStatus.FAULT, new BigDecimal("12.50"));
        DeviceDto result = deviceService.updateDeviceStatus(1L, request);

        assertThat(result.status()).isEqualTo(DeviceStatus.FAULT);
        assertThat(result.batteryLevel()).isEqualByComparingTo("12.50");
        verify(eventBroadcaster).broadcast(eq("DEVICE_STATUS_CHANGED"), any());
    }
}
