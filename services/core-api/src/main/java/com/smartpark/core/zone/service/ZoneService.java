package com.smartpark.core.zone.service;

import com.smartpark.core.alert.domain.AlertSeverity;
import com.smartpark.core.alert.domain.AlertStatus;
import com.smartpark.core.alert.repository.AlertRepository;
import com.smartpark.core.audit.service.AuditService;
import com.smartpark.core.common.exception.ResourceNotFoundException;
import com.smartpark.core.device.domain.Device;
import com.smartpark.core.device.domain.DeviceStatus;
import com.smartpark.core.device.repository.DeviceRepository;
import com.smartpark.core.park.domain.Park;
import com.smartpark.core.park.repository.ParkRepository;
import com.smartpark.core.zone.domain.Zone;
import com.smartpark.core.zone.dto.CreateZoneRequest;
import com.smartpark.core.zone.dto.ZoneDto;
import com.smartpark.core.zone.dto.ZoneHealthScoreDto;
import com.smartpark.core.zone.repository.ZoneRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final ParkRepository parkRepository;
    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;
    private final AuditService auditService;

    public ZoneService(
        ZoneRepository zoneRepository,
        ParkRepository parkRepository,
        DeviceRepository deviceRepository,
        AlertRepository alertRepository,
        AuditService auditService
    ) {
        this.zoneRepository = zoneRepository;
        this.parkRepository = parkRepository;
        this.deviceRepository = deviceRepository;
        this.alertRepository = alertRepository;
        this.auditService = auditService;
    }

    public List<ZoneDto> getZonesByPark(Long parkId) {
        return zoneRepository.findByParkId(parkId).stream()
            .map(ZoneDto::fromEntity)
            .toList();
    }

    public ZoneDto getZoneById(Long id) {
        Zone zone = findZoneEntity(id);
        return ZoneDto.fromEntity(zone);
    }

    @Transactional
    public ZoneDto createZone(Long parkId, CreateZoneRequest request) {
        Park park = parkRepository.findById(parkId)
            .orElseThrow(() -> new ResourceNotFoundException("Park", "id", parkId));

        Zone zone = new Zone(
            park,
            request.name(),
            request.zoneType(),
            request.polygonGeojson(),
            request.targetMoistureLevel()
        );
        Zone saved = zoneRepository.save(zone);
        auditService.logAction("CREATE", "ZONE", saved.getId(), "Created zone: " + saved.getName() + " in park: " + park.getName());
        return ZoneDto.fromEntity(saved);
    }

    @Transactional
    public void deleteZone(Long id) {
        Zone zone = findZoneEntity(id);
        zoneRepository.delete(zone);
        auditService.logAction("DELETE", "ZONE", id, "Deleted zone: " + zone.getName());
    }

    public ZoneHealthScoreDto calculateZoneHealthScore(Long zoneId) {
        Zone zone = findZoneEntity(zoneId);
        List<Device> devices = deviceRepository.findByZoneId(zoneId);

        int totalDevices = devices.size();
        int activeDevices = (int) devices.stream().filter(d -> d.getStatus() == DeviceStatus.ACTIVE).count();
        int faultDevices = totalDevices - activeDevices;

        long openAlerts = devices.stream()
            .flatMap(d -> alertRepository.findByDeviceId(d.getId()).stream())
            .filter(a -> a.getStatus() == AlertStatus.OPEN)
            .count();

        int score = 100;
        if (totalDevices > 0) {
            score -= (faultDevices * 15);
            score -= ((int) openAlerts * 10);
        }
        score = Math.max(0, Math.min(100, score));

        String verdict;
        if (score >= 90) verdict = "EXCELLENT";
        else if (score >= 75) verdict = "GOOD";
        else if (score >= 50) verdict = "ATTENTION_REQUIRED";
        else verdict = "CRITICAL";

        return new ZoneHealthScoreDto(
            zone.getId(),
            zone.getName(),
            score,
            totalDevices,
            activeDevices,
            faultDevices,
            (int) openAlerts,
            verdict
        );
    }

    public Zone findZoneEntity(Long id) {
        return zoneRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));
    }
}
