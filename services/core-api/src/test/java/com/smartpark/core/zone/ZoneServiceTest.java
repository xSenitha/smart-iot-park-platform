package com.smartpark.core.zone;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.smartpark.core.alert.repository.AlertRepository;
import com.smartpark.core.audit.service.AuditService;
import com.smartpark.core.device.domain.Device;
import com.smartpark.core.device.domain.DeviceStatus;
import com.smartpark.core.device.domain.DeviceType;
import com.smartpark.core.device.repository.DeviceRepository;
import com.smartpark.core.park.domain.Park;
import com.smartpark.core.park.repository.ParkRepository;
import com.smartpark.core.zone.domain.Zone;
import com.smartpark.core.zone.dto.ZoneHealthScoreDto;
import com.smartpark.core.zone.repository.ZoneRepository;
import com.smartpark.core.zone.service.ZoneService;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ParkRepository parkRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AuditService auditService;

    private ZoneService zoneService;

    @BeforeEach
    void setUp() {
        zoneService = new ZoneService(zoneRepository, parkRepository, deviceRepository, alertRepository, auditService);
    }

    @Test
    void testCalculateZoneHealthScore_AllHealthy() {
        Park park = new Park("Central Park", "Downtown", new BigDecimal("500000"), "UTC", "ACTIVE");
        park.setId(1L);

        Zone zone = new Zone(park, "Central Plaza", "PLAZA", null, new BigDecimal("40.00"));
        zone.setId(10L);

        Device dev1 = new Device(zone, "DEV-BIN-0001", "Bin 1", DeviceType.BIN, "X1", "v1.0");
        Device dev2 = new Device(zone, "DEV-LIGHT-0001", "Light 1", DeviceType.LIGHT, "L1", "v1.0");

        when(zoneRepository.findById(10L)).thenReturn(Optional.of(zone));
        when(deviceRepository.findByZoneId(10L)).thenReturn(List.of(dev1, dev2));
        when(alertRepository.findByDeviceId(any())).thenReturn(Collections.emptyList());

        ZoneHealthScoreDto score = zoneService.calculateZoneHealthScore(10L);

        assertThat(score.healthScorePercentage()).isEqualTo(100);
        assertThat(score.statusVerdict()).isEqualTo("EXCELLENT");
        assertThat(score.activeDevices()).isEqualTo(2);
        assertThat(score.faultDevices()).isEqualTo(0);
    }
}
