package com.smartpark.core.zone.controller;

import com.smartpark.core.common.dto.ApiResponse;
import com.smartpark.core.zone.dto.CreateZoneRequest;
import com.smartpark.core.zone.dto.ZoneDto;
import com.smartpark.core.zone.dto.ZoneHealthScoreDto;
import com.smartpark.core.zone.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Zones", description = "Endpoints for park zones, geographic polygons, and zone health analytics")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping("/api/v1/parks/{parkId}/zones")
    @Operation(summary = "List all zones belonging to a given park")
    public ResponseEntity<ApiResponse<List<ZoneDto>>> getZonesByPark(@PathVariable Long parkId) {
        return ResponseEntity.ok(ApiResponse.success(zoneService.getZonesByPark(parkId)));
    }

    @PostMapping("/api/v1/parks/{parkId}/zones")
    @PreAuthorize("hasAnyRole('ADMIN', 'CITY_PLANNER')")
    @Operation(summary = "Create a new zone within a park (Admin or City Planner)")
    public ResponseEntity<ApiResponse<ZoneDto>> createZone(
        @PathVariable Long parkId,
        @Valid @RequestBody CreateZoneRequest request
    ) {
        ZoneDto created = zoneService.createZone(parkId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Zone created successfully", created));
    }

    @GetMapping("/api/v1/zones/{id}")
    @Operation(summary = "Get detailed information for a specific zone")
    public ResponseEntity<ApiResponse<ZoneDto>> getZoneById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(zoneService.getZoneById(id)));
    }

    @GetMapping("/api/v1/zones/{id}/health-score")
    @Operation(summary = "Compute and retrieve live environmental and asset health score for a zone")
    public ResponseEntity<ApiResponse<ZoneHealthScoreDto>> getZoneHealthScore(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(zoneService.calculateZoneHealthScore(id)));
    }

    @DeleteMapping("/api/v1/zones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a zone and its assigned devices (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteZone(@PathVariable Long id) {
        zoneService.deleteZone(id);
        return ResponseEntity.ok(ApiResponse.success("Zone deleted successfully", null));
    }
}
