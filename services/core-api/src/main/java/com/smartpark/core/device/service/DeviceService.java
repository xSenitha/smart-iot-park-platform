package com.smartpark.core.device.service;

import com.smartpark.core.audit.service.AuditService;
import com.smartpark.core.common.exception.BadRequestException;
import com.smartpark.core.common.exception.ResourceNotFoundException;
import com.smartpark.core.device.domain.Device;
import com.smartpark.core.device.domain.DeviceStatus;
import com.smartpark.core.device.domain.DeviceType;
import com.smartpark.core.device.dto.CreateDeviceRequest;
import com.smartpark.core.device.dto.DeviceDto;
import com.smartpark.core.device.dto.HeartbeatRequest;
import com.smartpark.core.device.dto.UpdateDeviceStatusRequest;
import com.smartpark.core.device.repository.DeviceRepository;
import com.smartpark.core.event.SseEventBroadcaster;
import com.smartpark.core.zone.domain.Zone;
import com.smartpark.core.zone.repository.ZoneRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final ZoneRepository zoneRepository;
    private final AuditService auditService;
    private final SseEventBroadcaster eventBroadcaster;

    public DeviceService(
        DeviceRepository deviceRepository,
        ZoneRepository zoneRepository,
        AuditService auditService,
        SseEventBroadcaster eventBroadcaster
    ) {
        this.deviceRepository = deviceRepository;
        this.zoneRepository = zoneRepository;
        this.auditService = auditService;
        this.eventBroadcaster = eventBroadcaster;
    }

    public List<DeviceDto> getDevices(Long parkId, Long zoneId, DeviceType type, DeviceStatus status) {
        List<Device> list;
        if (zoneId != null) {
            list = deviceRepository.findByZoneId(zoneId);
        } else if (parkId != null) {
            list = deviceRepository.findByParkId(parkId);
        } else if (type != null) {
            list = deviceRepository.findByType(type);
        } else if (status != null) {
            list = deviceRepository.findByStatus(status);
        } else {
            list = deviceRepository.findAll();
        }

        return list.stream().map(DeviceDto::fromEntity).toList();
    }

    public DeviceDto getDeviceById(Long id) {
        Device device = findDeviceEntity(id);
        return DeviceDto.fromEntity(device);
    }

    public DeviceDto getDeviceByUid(String uid) {
        Device device = deviceRepository.findByDeviceUid(uid)
            .orElseThrow(() -> new ResourceNotFoundException("Device", "deviceUid", uid));
        return DeviceDto.fromEntity(device);
    }

    @Transactional
    public DeviceDto createDevice(CreateDeviceRequest request) {
        if (deviceRepository.findByDeviceUid(request.deviceUid()).isPresent()) {
            throw new BadRequestException("Device UID is already registered: " + request.deviceUid());
        }

        Zone zone = zoneRepository.findById(request.zoneId())
            .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", request.zoneId()));

        Device device = new Device(
            zone,
            request.deviceUid(),
            request.name(),
            request.type(),
            request.model(),
            request.firmwareVersion()
        );

        Device saved = deviceRepository.save(device);
        auditService.logAction("CREATE", "DEVICE", saved.getId(), "Registered device: " + saved.getDeviceUid());
        eventBroadcaster.broadcast("DEVICE_REGISTERED", DeviceDto.fromEntity(saved));
        return DeviceDto.fromEntity(saved);
    }

    @Transactional
    public DeviceDto updateDeviceStatus(Long id, UpdateDeviceStatusRequest request) {
        Device device = findDeviceEntity(id);
        device.setStatus(request.status());
        if (request.batteryLevel() != null) {
            device.setBatteryLevel(request.batteryLevel());
        }

        Device updated = deviceRepository.save(device);
        auditService.logAction("UPDATE_STATUS", "DEVICE", updated.getId(), "Status changed to: " + request.status());
        eventBroadcaster.broadcast("DEVICE_STATUS_CHANGED", DeviceDto.fromEntity(updated));
        return DeviceDto.fromEntity(updated);
    }

    @Transactional
    public void recordHeartbeat(String deviceUid, HeartbeatRequest request) {
        deviceRepository.findByDeviceUid(deviceUid).ifPresent(device -> {
            device.setLastHeartbeat(Instant.now());
            if (request != null && request.batteryLevel() != null) {
                device.setBatteryLevel(request.batteryLevel());
            }
            if (request != null && request.firmwareVersion() != null) {
                device.setFirmwareVersion(request.firmwareVersion());
            }
            deviceRepository.save(device);
        });
    }

    @Transactional
    public void deleteDevice(Long id) {
        Device device = findDeviceEntity(id);
        deviceRepository.delete(device);
        auditService.logAction("DELETE", "DEVICE", id, "Removed device: " + device.getDeviceUid());
    }

    public Device findDeviceEntity(Long id) {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
    }
}
