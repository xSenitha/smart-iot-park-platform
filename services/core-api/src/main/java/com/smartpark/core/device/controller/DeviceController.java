package com.smartpark.core.device.controller;

import com.smartpark.core.common.dto.ApiResponse;
import com.smartpark.core.device.domain.DeviceStatus;
import com.smartpark.core.device.domain.DeviceType;
import com.smartpark.core.device.dto.CreateDeviceRequest;
import com.smartpark.core.device.dto.DeviceDto;
import com.smartpark.core.device.dto.HeartbeatRequest;
import com.smartpark.core.device.dto.UpdateDeviceStatusRequest;
import com.smartpark.core.device.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Devices", description = "Endpoints for IoT device registry, statuses, and edge heartbeat telemetry")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @Operation(summary = "Search devices with optional filters (parkId, zoneId, type, status)")
    public ResponseEntity<ApiResponse<List<DeviceDto>>> getDevices(
        @RequestParam(required = false) Long parkId,
        @RequestParam(required = false) Long zoneId,
        @RequestParam(required = false) DeviceType type,
        @RequestParam(required = false) DeviceStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getDevices(parkId, zoneId, type, status)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed device information by primary key ID")
    public ResponseEntity<ApiResponse<DeviceDto>> getDeviceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getDeviceById(id)));
    }

    @GetMapping("/uid/{uid}")
    @Operation(summary = "Get detailed device information by hardware unique identifier")
    public ResponseEntity<ApiResponse<DeviceDto>> getDeviceByUid(@PathVariable String uid) {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getDeviceByUid(uid)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CITY_PLANNER')")
    @Operation(summary = "Provision a new device into a park zone (Admin or City Planner)")
    public ResponseEntity<ApiResponse<DeviceDto>> createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        DeviceDto created = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Device registered successfully", created));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTENANCE_CREW')")
    @Operation(summary = "Transition device operational status (Admin or Maintenance Crew)")
    public ResponseEntity<ApiResponse<DeviceDto>> updateDeviceStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateDeviceStatusRequest request
    ) {
        DeviceDto updated = deviceService.updateDeviceStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Device status updated successfully", updated));
    }

    @PostMapping("/uid/{uid}/heartbeat")
    @Operation(summary = "Internal telemetry ingestion endpoint to record device heartbeat and battery level")
    public ResponseEntity<ApiResponse<Void>> recordHeartbeat(
        @PathVariable String uid,
        @RequestBody(required = false) HeartbeatRequest request
    ) {
        deviceService.recordHeartbeat(uid, request);
        return ResponseEntity.ok(ApiResponse.success("Heartbeat recorded", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Decommission and remove device (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(ApiResponse.success("Device removed successfully", null));
    }
}
